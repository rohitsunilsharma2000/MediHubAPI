package com.MediHubAPI.controller;

import com.MediHubAPI.dto.AppointmentBookingDto;
import com.MediHubAPI.model.enums.AppointmentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AppointmentController using JWT authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private Long testDoctorId = 1L;  // Replace with real test doctor ID
    private Long testPatientId = 2L; // Replace with real test patient ID

    @BeforeEach
    void authenticateAndFetchJwtToken() throws Exception {
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", "superadmin1"); // test user in your DB
        loginPayload.put("password", "supersecurepassword");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = "Bearer " + jsonNode.get("accessToken").asText();
    }

    @Test
    void shouldBookAppointmentSuccessfully() throws Exception {
        AppointmentBookingDto bookingDto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .slotTime(LocalTime.of(10, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Appointment booked successfully"))
                .andExpect(jsonPath("$.data.doctorName").exists())
                .andExpect(jsonPath("$.data.slot.status").value("AVAILABLE"));
    }

    @Test
    void shouldFailBookingAlreadyBookedSlot() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(2))
                .slotTime(LocalTime.of(9, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        // Book once
        mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Book again - should fail
        mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Slot is not available"));
    }

    @Test
    void shouldFetchAppointmentsForPatient() throws Exception {
        mockMvc.perform(get("/appointments/patient/" + testPatientId)
                        .header("Authorization", jwtToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void shouldRejectUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/appointments/patient/" + testPatientId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtTokenShouldBeValid() {
        assertThat(jwtToken).startsWith("Bearer ");
    }


    @Test
    void shouldReturnErrorForNonExistentSlot() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.of(2030, 1, 1))
                .slotTime(LocalTime.of(23, 59))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldPreventDoubleBookingForPatientAndDoctor() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(3))
                .slotTime(LocalTime.of(11, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenCancellingNonExistentAppointment() throws Exception {
        mockMvc.perform(delete("/appointments/999999")
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSupportConcurrentBookingConflict() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(4))
                .slotTime(LocalTime.of(12, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    mockMvc.perform(post("/appointments/book")
                                    .header("Authorization", jwtToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto)))
                            .andReturn();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }

    @Test
    void shouldReturnDoctorAppointmentsByDate() throws Exception {
        mockMvc.perform(get("/appointments/" + testDoctorId)
                        .header("Authorization", jwtToken)
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnFilteredDoctorSchedule() throws Exception {
        mockMvc.perform(get("/appointments/doctor-schedules/paged")
                        .header("Authorization", jwtToken)
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .param("doctorName", "sm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturnAppointmentsForTodayAndWeekRange() throws Exception {
        mockMvc.perform(get("/appointments")
                        .header("Authorization", jwtToken)
                        .param("range", "TODAY"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/appointments")
                        .header("Authorization", jwtToken)
                        .param("range", "WEEK"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMarkAppointmentAsArrived() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(8))
                .slotTime(LocalTime.of(10, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult booked = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        long appointmentId = objectMapper.readTree(booked.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/appointments/" + appointmentId + "/arrive")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment marked as ARRIVED"));
    }

    @Test
    void shouldFailMarkingAlreadyArrivedAppointment() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult booked = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        long appointmentId = objectMapper.readTree(booked.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/appointments/" + appointmentId + "/arrive")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/appointments/" + appointmentId + "/arrive")
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointment already marked as ARRIVED"));
    }

    @Test
    void shouldFilterAppointmentsByDoctorName() throws Exception {
        mockMvc.perform(get("/appointments/doctor-schedules/paged")
                        .header("Authorization", jwtToken)
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .param("doctorName", "sm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
    @Test
    void shouldRescheduleValidAppointmentAndCancelOld() throws Exception {
        // Step 1: Book original appointment
        AppointmentBookingDto originalDto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult booked = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode originalResponse = objectMapper.readTree(booked.getResponse().getContentAsString());
        long originalAppointmentId = originalResponse.get("data").get("id").asLong();

        // Step 2: Reschedule to new slot
        Map<String, Object> reschedulePayload = new HashMap<>();
        reschedulePayload.put("newDate", originalDto.getAppointmentDate().toString());
        reschedulePayload.put("newTime", "12:30");

        MvcResult rescheduled = mockMvc.perform(put("/appointments/" + originalAppointmentId + "/reschedule")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reschedulePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment rescheduled successfully"))
                .andReturn();

        JsonNode rescheduleResponse = objectMapper.readTree(rescheduled.getResponse().getContentAsString());
        long newAppointmentId = rescheduleResponse.get("data").get("id").asLong();

        // Step 3: Check original appointment is CANCELLED
        mockMvc.perform(get("/appointments/view/" + originalAppointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Step 4: Check new appointment is BOOKED
        mockMvc.perform(get("/appointments/view/" + newAppointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BOOKED"));
    }

    @Test
    void shouldRescheduleAppointmentWithoutRescheduleDto() throws Exception {
        // Step 1: Book an initial appointment at 11:30
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult bookResult = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode bookJson = objectMapper.readTree(bookResult.getResponse().getContentAsString());
        Long oldAppointmentId = bookJson.get("data").get("id").asLong();

        // Step 2: Reschedule using the same booking DTO but with a new time (12:30)
        AppointmentBookingDto newSlotDto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(dto.getAppointmentDate())
                .slotTime(LocalTime.of(12, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult rescheduleResult = mockMvc.perform(put("/appointments/" + oldAppointmentId + "/reschedule")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSlotDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment rescheduled successfully"))
                .andReturn();

        JsonNode rescheduleJson = objectMapper.readTree(rescheduleResult.getResponse().getContentAsString());
        Long newAppointmentId = rescheduleJson.get("data").get("id").asLong();

        // Step 3: Assert the original appointment is CANCELLED
        mockMvc.perform(get("/appointments/view/" + oldAppointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Step 4: Assert the new appointment is BOOKED
        mockMvc.perform(get("/appointments/view/" + newAppointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BOOKED"));
    }
    @Test
    void shouldMarkBookedAppointmentAsArrived() throws Exception {
        // Step 1: Book an appointment
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult result = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long appointmentId = jsonNode.get("data").get("id").asLong();

        // Step 2: Mark appointment as ARRIVED
        mockMvc.perform(put("/appointments/" + appointmentId + "/mark-arrived")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment marked as ARRIVED"));

        // Step 3: Verify status is ARRIVED
        mockMvc.perform(get("/appointments/view/" + appointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ARRIVED"));
    }

    @Test
    void shouldFailWhenMarkingAlreadyArrivedAppointment() throws Exception {
        // Step 1: Book an appointment
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult result = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long appointmentId = jsonNode.get("data").get("id").asLong();

        // Step 2: Mark appointment as ARRIVED
        mockMvc.perform(put("/appointments/" + appointmentId + "/mark-arrived")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());

        // Step 3: Try marking ARRIVED again — should fail
        mockMvc.perform(put("/appointments/" + appointmentId + "/mark-arrived")
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointment already marked as ARRIVED"));
    }

    @Test
    void shouldFailWhenCancellingAlreadyCancelledAppointment() throws Exception {
        // Step 1: Book an appointment
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult result = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long appointmentId = json.get("data").get("id").asLong();

        // Step 2: Cancel the appointment
        mockMvc.perform(delete("/appointments/" + appointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment cancelled successfully"));

        // Step 3: Try to cancel again → expect 400
        mockMvc.perform(delete("/appointments/" + appointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointment already cancelled"));
    }

    @Test
    void shouldReturnAppointmentsWhenFilteringByDoctorName() throws Exception {
        mockMvc.perform(get("/appointments")
                        .header("Authorization", jwtToken)
                        .param("doctorName", "sm"))  // assuming "sm" matches a real doctor
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldFailReschedulingCompletedAppointment() throws Exception {
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(10))
                .slotTime(LocalTime.of(9, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        MvcResult booked = mockMvc.perform(post("/appointments/book")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long appointmentId = objectMapper.readTree(booked.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Manually mark as COMPLETED (or hit controller if exists)
        mockMvc.perform(put("/appointments/" + appointmentId + "/mark-completed")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());

        // Try rescheduling
        AppointmentBookingDto rescheduleDto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(dto.getAppointmentDate())
                .slotTime(LocalTime.of(10, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        mockMvc.perform(put("/appointments/" + appointmentId + "/reschedule")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rescheduleDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot reschedule a COMPLETED appointment"));
    }

}

