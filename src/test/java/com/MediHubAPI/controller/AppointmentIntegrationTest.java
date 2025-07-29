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
import java.util.*;
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

    @Test
    void shouldSupportConcurrentBookingConflictV2() throws Exception {
        // Prepare the common booking DTO (same doctor, patient, time)
        AppointmentBookingDto dto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(LocalDate.now().plusDays(9))
                .slotTime(LocalTime.of(11, 30))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        // Thread setup
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Ensure all threads hit at same time
                    MvcResult result = mockMvc.perform(post("/appointments/book")
                                    .header("Authorization", jwtToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto)))
                            .andReturn();

                    statuses.add(result.getResponse().getStatus());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Let both threads proceed
        doneLatch.await(); // Wait for both to finish

        // Analyze responses
        long successCount = statuses.stream().filter(status -> status == 201).count();
        long conflictCount = statuses.stream().filter(status -> status == 409).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(1);
    }


    //2	One thread books and another cancels the same appointment concurrently	One wins; status reflects
    // final state	/appointments/book, /appointments/{id}	Use synchronized lock or optimistic locking/**/
    @Test
    void shouldHandleConcurrentBookAndCancelOnSameAppointment() throws Exception {
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

        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Step 2: Setup concurrency: one will cancel, one will mark COMPLETED
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        List<String> responses = Collections.synchronizedList(new ArrayList<>());

        // Thread A - CANCEL
        executor.submit(() -> {
            try {
                startLatch.await();
                MvcResult cancelResult = mockMvc.perform(delete("/appointments/" + appointmentId)
                                .header("Authorization", jwtToken))
                        .andReturn();
                responses.add("CANCEL-" + cancelResult.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        // Thread B - MARK COMPLETED
        executor.submit(() -> {
            try {
                startLatch.await();
                MvcResult completeResult = mockMvc.perform(put("/appointments/" + appointmentId + "/mark-completed")
                                .header("Authorization", jwtToken))
                        .andReturn();
                responses.add("COMPLETE-" + completeResult.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        // Trigger both threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        // Print results for debug
        responses.forEach(System.out::println);

        // Ensure at least one succeeds and one fails (depends on locking mechanism in service)
        long successCount = responses.stream().filter(r -> r.endsWith("200")).count();
        long errorCount = responses.stream().filter(r -> r.startsWith("CANCEL-400") || r.startsWith("COMPLETE-400")).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(errorCount).isEqualTo(1);
    }


    //3	One reschedules an appointment while another tries to arrive the same	One fails with 400
    //    BAD_REQUEST	/appointments/{id}/reschedule, /arrive	State transition validation required

    @Test
    void shouldHandleConcurrentRescheduleAndArriveAttempt() throws Exception {
        // Step 1: Book an appointment first
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

        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Step 2: Setup concurrency
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Thread A: Reschedule to new time
        executor.submit(() -> {
            try {
                startLatch.await();
                Map<String, Object> reschedulePayload = new HashMap<>();
                reschedulePayload.put("appointmentDate", dto.getAppointmentDate().toString());
                reschedulePayload.put("slotTime", "12:00");

                MvcResult res = mockMvc.perform(put("/appointments/" + appointmentId + "/reschedule")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reschedulePayload)))
                        .andReturn();
                results.add("RESCHEDULE-" + res.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        // Thread B: Try to mark as ARRIVED
        executor.submit(() -> {
            try {
                startLatch.await();
                MvcResult res = mockMvc.perform(put("/appointments/" + appointmentId + "/arrive")
                                .header("Authorization", jwtToken))
                        .andReturn();
                results.add("ARRIVE-" + res.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        // Trigger both threads
        startLatch.countDown();
        doneLatch.await();

        // Show results
        results.forEach(System.out::println);

        // Assert only one succeeded with 200
        long success = results.stream().filter(r -> r.endsWith("200")).count();
        long failure = results.stream().filter(r -> r.endsWith("400")).count();

        assertThat(success).isEqualTo(1);
        assertThat(failure).isEqualTo(1);
    }

    /**
     * 4	Admin tries to block a slot while a patient is booking it	Blocking should fail or vice versa
     * SlotService & /book	Add atomic operation checks in SlotService
     */

    @Test
    void shouldHandleConcurrentBookingAndBlockingAttempt() throws Exception {
        // Book/Block target slot
        LocalDate date = LocalDate.now().plusDays(9);
        LocalTime time = LocalTime.of(11, 30);

        AppointmentBookingDto bookingDto = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(date)
                .slotTime(time)
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        Map<String, Object> blockSlotPayload = new HashMap<>();
        blockSlotPayload.put("doctorId", testDoctorId);
        blockSlotPayload.put("date", date.toString());
        blockSlotPayload.put("slotTime", time.toString());

        // Concurrency Setup
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Thread A: Book
        executor.submit(() -> {
            try {
                latch.await();
                MvcResult result = mockMvc.perform(post("/appointments/book")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookingDto)))
                        .andReturn();
                results.add("BOOK-" + result.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        });

        // Thread B: Block
        executor.submit(() -> {
            try {
                latch.await();
                MvcResult result = mockMvc.perform(put("/slots/block")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(blockSlotPayload)))
                        .andReturn();
                results.add("BLOCK-" + result.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        });

        // Trigger both threads
        latch.countDown();
        done.await();

        results.forEach(System.out::println);

        // Verify: 1 succeeded, 1 failed
        long success = results.stream().filter(r -> r.endsWith("200") || r.endsWith("201")).count();
        long conflictOrFail = results.stream().filter(r -> r.endsWith("400") || r.endsWith("409")).count();

        assertThat(success).isEqualTo(1);
        assertThat(conflictOrFail).isEqualTo(1);
    }
    /**
     * 5	Same patient books 2 different slots at same time	Allow only one if same date-time
     * /book	Conflict for patient overlapping time
     */
    @Test
    void shouldPreventSamePatientFromDoubleBookingAtSameTime() throws Exception {
        LocalDate date = LocalDate.now().plusDays(9);
        LocalTime time = LocalTime.of(11, 30);

        // Two booking DTOs with different doctorIds but same patient, date, time
        AppointmentBookingDto booking1 = AppointmentBookingDto.builder()
                .doctorId(testDoctorId)
                .patientId(testPatientId)
                .appointmentDate(date)
                .slotTime(time)
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        AppointmentBookingDto booking2 = AppointmentBookingDto.builder()
                .doctorId(testDoctorId + 1) // Assume another valid doctor
                .patientId(testPatientId)
                .appointmentDate(date)
                .slotTime(time)
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        executor.submit(() -> {
            try {
                latch.await();
                MvcResult result = mockMvc.perform(post("/appointments/book")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking1)))
                        .andReturn();
                results.add("BOOK1-" + result.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        });

        executor.submit(() -> {
            try {
                latch.await();
                MvcResult result = mockMvc.perform(post("/appointments/book")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking2)))
                        .andReturn();
                results.add("BOOK2-" + result.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        });

        latch.countDown();
        done.await();

        results.forEach(System.out::println);

        long success = results.stream().filter(r -> r.endsWith("201")).count();
        long conflict = results.stream().filter(r -> r.endsWith("409")).count();

        assertThat(success).isEqualTo(1);
        assertThat(conflict).isEqualTo(1);
    }

    /**
     * 6	Reschedule happens twice in parallel	Only one should succeed, other should be blocked or rejected
     * /reschedule	Use DB transaction isolation or version checks
     */

    @Test
    void shouldAllowOnlyOneSuccessfulRescheduleInParallel() throws Exception {
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
        long appointmentId = json.get("data").get("id").asLong();

        // Two new slots to reschedule to
        LocalTime rescheduleTime1 = LocalTime.of(12, 0);
        LocalTime rescheduleTime2 = LocalTime.of(12, 30);

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<String> outcomes = Collections.synchronizedList(new ArrayList<>());

        Runnable task1 = () -> {
            try {
                latch.await();
                MvcResult r1 = mockMvc.perform(put("/appointments/" + appointmentId + "/reschedule")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"newDate\": \"" + dto.getAppointmentDate() + "\", \"newTime\": \"" + rescheduleTime1 + "\"}"))
                        .andReturn();
                outcomes.add("T1-" + r1.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                latch.await();
                MvcResult r2 = mockMvc.perform(put("/appointments/" + appointmentId + "/reschedule")
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"newDate\": \"" + dto.getAppointmentDate() + "\", \"newTime\": \"" + rescheduleTime2 + "\"}"))
                        .andReturn();
                outcomes.add("T2-" + r2.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);
        latch.countDown();
        done.await();

        outcomes.forEach(System.out::println);

        long success = outcomes.stream().filter(o -> o.endsWith("200")).count();
        long conflict = outcomes.stream().filter(o -> o.endsWith("400") || o.endsWith("409")).count();

        assertThat(success).isEqualTo(1);
        assertThat(conflict).isEqualTo(1);
    }

    /**
     * 7	Arrival and cancel run simultaneously	One should succeed; use transaction to rollback the other
     * /arrive, /cancel	Add locking/transactional isolation
     */
    @Test
    void shouldAllowOnlyOneBetweenArrivalAndCancelInParallel() throws Exception {
        // Step 1: Book appointment
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

        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<String> outcomes = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task 1: Arrival
        Runnable arriveTask = () -> {
            try {
                latch.await();
                MvcResult arriveResult = mockMvc.perform(put("/appointments/" + appointmentId + "/arrive")
                                .header("Authorization", jwtToken))
                        .andReturn();
                outcomes.add("ARRIVE-" + arriveResult.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        };

        // Task 2: Cancel
        Runnable cancelTask = () -> {
            try {
                latch.await();
                MvcResult cancelResult = mockMvc.perform(delete("/appointments/" + appointmentId)
                                .header("Authorization", jwtToken))
                        .andReturn();
                outcomes.add("CANCEL-" + cancelResult.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        };

        executor.submit(arriveTask);
        executor.submit(cancelTask);
        latch.countDown();
        done.await();

        outcomes.forEach(System.out::println);

        long success = outcomes.stream().filter(o -> o.endsWith("200")).count();
        long fail = outcomes.stream().filter(o -> o.endsWith("400") || o.endsWith("409")).count();

        assertThat(success).isEqualTo(1);
        assertThat(fail).isEqualTo(1);
    }

    /**
     * 8	Same slot marked completed and cancelled in parallel	Ensure state consistency
     * /complete, /cancel	Final state should not be ambiguous
     */
    @Test
    void shouldNotAllowCancelAndCompleteTogether() throws Exception {
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

        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable completeTask = () -> {
            try {
                startLatch.await();
                MvcResult response = mockMvc.perform(put("/appointments/" + appointmentId + "/mark-completed")
                                .header("Authorization", jwtToken))
                        .andReturn();
                results.add("COMPLETE-" + response.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        };

        Runnable cancelTask = () -> {
            try {
                startLatch.await();
                MvcResult response = mockMvc.perform(delete("/appointments/" + appointmentId)
                                .header("Authorization", jwtToken))
                        .andReturn();
                results.add("CANCEL-" + response.getResponse().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(completeTask);
        executor.submit(cancelTask);

        startLatch.countDown();
        doneLatch.await();

        results.forEach(System.out::println);

        long successCount = results.stream().filter(r -> r.endsWith("200")).count();
        long failCount = results.stream().filter(r -> r.endsWith("400") || r.endsWith("409")).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(1);

        // Fetch final state and assert
        MvcResult finalResult = mockMvc.perform(get("/appointments/view/" + appointmentId)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String finalStatus = objectMapper.readTree(finalResult.getResponse().getContentAsString())
                .get("data").get("status").asText();

        assertThat(finalStatus).isIn("CANCELLED", "COMPLETED");
    }

}

//https://chatgpt.com/share/688901d5-c104-8009-88d8-223bd3da877d