
package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.AppointmentBookingDto;
import com.MediHubAPI.dto.AppointmentResponseDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.Slot;
import com.MediHubAPI.model.User;
import com.MediHubAPI.model.enums.AppointmentStatus;
import com.MediHubAPI.model.enums.AppointmentType;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.repository.AppointmentRepository;
import com.MediHubAPI.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;


    @Mock
    private UserRepository userRepository;

    @Mock
    private SlotServiceImpl slotService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User doctor;
    private User patient;
    private Slot slot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doctor = new User();
        doctor.setId(1L);
        patient = new User();
        patient.setId(2L);
        slot = new Slot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(10, 30));
    }




    private final Long doctorId = 1L;
    private final Long patientId = 2L;
    private final LocalDate date = LocalDate.of(2025, 8, 1);
    private final LocalTime time = LocalTime.of(10, 0);

    private AppointmentBookingDto buildBookingDto() {
        return AppointmentBookingDto.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .appointmentDate(date)
                .slotTime(time)
                .appointmentType(AppointmentType.IN_PERSON)
                .build();
    }

    @Test
    void shouldBookAppointmentSuccessfully() {
        AppointmentBookingDto dto = buildBookingDto();

        User doctor = new User(); doctor.setId(doctorId);
        User patient = new User(); patient.setId(patientId);

        Slot slot = Slot.builder()
                .doctor(doctor)
                .startTime(time)
                .endTime(time.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        Appointment appointment = Appointment.builder()
                .id(1L)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(date)
                .slotTime(time)
                .status(AppointmentStatus.BOOKED)
                .build();

        AppointmentResponseDto responseDto = new AppointmentResponseDto();

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(slotService.getSlotByDoctorAndTime(doctorId, date, time)).thenReturn(slot);
        when(appointmentRepository.existsByDoctorAndAppointmentDateAndSlotTime(doctor, date, time)).thenReturn(false);
        when(appointmentRepository.existsByPatientAndAppointmentDateAndSlotTime(patient, date, time)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(modelMapper.map(eq(appointment), eq(AppointmentResponseDto.class))).thenReturn(responseDto);

        AppointmentResponseDto result = appointmentService.bookAppointment(dto);
        assertNotNull(result);
    }

    @Test
    void shouldThrowIfSlotIsNotAvailable() {
        AppointmentBookingDto dto = buildBookingDto();

        User doctor = new User(); doctor.setId(doctorId);
        User patient = new User(); patient.setId(patientId);

        Slot slot = Slot.builder().status(SlotStatus.BLOCKED).build();

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(slotService.getSlotByDoctorAndTime(doctorId, date, time)).thenReturn(slot);

        HospitalAPIException ex = assertThrows(HospitalAPIException.class,
                () -> appointmentService.bookAppointment(dto));

        assertEquals("Slot is not available", ex.getMessage());
    }

    @Test
    void shouldThrowIfDoctorHasConflict() {
        AppointmentBookingDto dto = buildBookingDto();

        User doctor = new User(); doctor.setId(doctorId);
        User patient = new User(); patient.setId(patientId);

        Slot slot = Slot.builder().status(SlotStatus.AVAILABLE).build();

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(slotService.getSlotByDoctorAndTime(doctorId, date, time)).thenReturn(slot);
        when(appointmentRepository.existsByDoctorAndAppointmentDateAndSlotTime(doctor, date, time)).thenReturn(true);

        HospitalAPIException ex = assertThrows(HospitalAPIException.class,
                () -> appointmentService.bookAppointment(dto));

        assertEquals("Doctor already has an appointment in this slot", ex.getMessage());
    }
    @Test
    void shouldThrowIfPatientHasConflict() {
        AppointmentBookingDto dto = buildBookingDto();

        User doctor = new User(); doctor.setId(doctorId);
        User patient = new User(); patient.setId(patientId);

        Slot slot = Slot.builder().status(SlotStatus.AVAILABLE).build();

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(slotService.getSlotByDoctorAndTime(doctorId, date, time)).thenReturn(slot);
        when(appointmentRepository.existsByDoctorAndAppointmentDateAndSlotTime(doctor, date, time)).thenReturn(false);
        when(appointmentRepository.existsByPatientAndAppointmentDateAndSlotTime(patient, date, time)).thenReturn(true);

        HospitalAPIException ex = assertThrows(HospitalAPIException.class,
                () -> appointmentService.bookAppointment(dto));

        assertEquals("Patient already has an appointment in this slot", ex.getMessage());
    }
    @Test
    void shouldLinkRescheduledAppointmentCorrectly() {
        AppointmentBookingDto dto = buildBookingDto();

        User doctor = new User(); doctor.setId(doctorId);
        User patient = new User(); patient.setId(patientId);

        Appointment rescheduledFrom = new Appointment();
        rescheduledFrom.setId(999L);

        Slot slot = Slot.builder().status(SlotStatus.AVAILABLE).build();
        Appointment newAppointment = Appointment.builder().id(1L).rescheduledFrom(rescheduledFrom).build();

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(slotService.getSlotByDoctorAndTime(doctorId, date, time)).thenReturn(slot);
        when(appointmentRepository.existsByDoctorAndAppointmentDateAndSlotTime(doctor, date, time)).thenReturn(false);
        when(appointmentRepository.existsByPatientAndAppointmentDateAndSlotTime(patient, date, time)).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(newAppointment);
        when(modelMapper.map(any(), eq(AppointmentResponseDto.class))).thenReturn(new AppointmentResponseDto());

        AppointmentResponseDto result = appointmentService.bookAppointment(dto, rescheduledFrom);
        assertNotNull(result);
    }

    //⚠️ 6. Test: Concurrency (simulate race condition)
    @Test
    void shouldHandleConcurrentBookingGracefully() throws InterruptedException {
        AppointmentBookingDto dto = buildBookingDto();

        User doctor = new User(); doctor.setId(doctorId);
        User patient = new User(); patient.setId(patientId);
        Slot slot = Slot.builder().status(SlotStatus.AVAILABLE).build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(doctor), Optional.of(patient));
        when(slotService.getSlotByDoctorAndTime(anyLong(), any(), any())).thenReturn(slot);
        when(appointmentRepository.existsByDoctorAndAppointmentDateAndSlotTime(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsByPatientAndAppointmentDateAndSlotTime(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenAnswer(invocation -> {
            Thread.sleep(50); // Simulate slow DB save
            return Appointment.builder().id(1L).build();
        });
        when(modelMapper.map(any(), eq(AppointmentResponseDto.class))).thenReturn(new AppointmentResponseDto());

        Runnable task = () -> {
            try {
                appointmentService.bookAppointment(dto);
            } catch (Exception ignored) {}
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start(); t2.start();
        t1.join(); t2.join();

        verify(appointmentRepository, atLeastOnce()).save(any());
    }



}