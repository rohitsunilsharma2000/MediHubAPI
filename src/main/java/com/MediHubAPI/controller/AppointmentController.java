package com.MediHubAPI.controller;

import com.MediHubAPI.dto.AppointmentBookingDto;
import com.MediHubAPI.dto.AppointmentResponseDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentBookingDto bookingDto) {
        try {
            AppointmentResponseDto booked = appointmentService.bookAppointment(bookingDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(booked);
        } catch (Exception e) {
            log.error("Unexpected error booking appointment", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to book appointment");
        }
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AppointmentResponseDto> list = appointmentService.getAppointmentsForDoctor(doctorId, date);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Error fetching doctor appointments", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch appointments");
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(patientId));
        } catch (Exception e) {
            log.error("Error fetching patient appointments", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch appointments");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            appointmentService.cancelAppointment(id);
            return ResponseEntity.ok("Appointment cancelled.");
        } catch (Exception e) {
            log.error("Error cancelling appointment", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel appointment");
        }
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(@PathVariable Long id,
                                        @Valid @RequestBody AppointmentBookingDto dto) {
        try {
            return ResponseEntity.ok(appointmentService.reschedule(id, dto));
        } catch (Exception e) {
            log.error("Error rescheduling appointment", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reschedule");
        }
    }
}
