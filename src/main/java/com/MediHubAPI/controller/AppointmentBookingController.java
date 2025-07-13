package com.MediHubAPI.controller;

import com.MediHubAPI.dto.availability.AdditionalSlotDTO;
import com.MediHubAPI.dto.booking.*;
import com.MediHubAPI.service.AppointmentBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentBookingController {

    private final AppointmentBookingService bookingService;

    // Get available slots
    @GetMapping("/slots/available")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(doctorId, date));
    }

    // Book a slot
    @PostMapping("/booking")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentBookingRequestDTO dto) {
        return ResponseEntity.ok(bookingService.bookAppointment(dto));
    }

    // Cancel booking
    @PutMapping("/booking/cancel")
    public ResponseEntity<?> cancelAppointment(@Valid @RequestBody AppointmentCancelDTO dto) {
        return ResponseEntity.ok(bookingService.cancelAppointment(dto));
    }

    // Mark as ARRIVED
    @PutMapping("/booking/arrive")
    public ResponseEntity<?> markArrived(@Valid @RequestBody AppointmentArriveDTO dto) {
        return ResponseEntity.ok(bookingService.markArrived(dto));
    }

    // Mark as COMPLETED
    @PutMapping("/booking/complete")
    public ResponseEntity<?> markCompleted(@Valid @RequestBody AppointmentCompleteDTO dto) {
        return ResponseEntity.ok(bookingService.markCompleted(dto));
    }

    // Mark as NO_SHOW
    @PutMapping("/booking/no-show")
    public ResponseEntity<?> markNoShow(@Valid @RequestBody AppointmentNoShowDTO dto) {
        return ResponseEntity.ok(bookingService.markNoShow(dto));
    }

    // Assign walk-in priority
    @PutMapping("/slots/prioritize")
    public ResponseEntity<?> assignWalkInPriority(@Valid @RequestBody WalkInPriorityDTO dto) {
        return ResponseEntity.ok(bookingService.assignWalkInPriority(dto));
    }

    // Emergency overbooking (add new slot)
    @PostMapping("/slots/emergency-add")
    public ResponseEntity<?> emergencySlotAdd(@Valid @RequestBody AdditionalSlotDTO dto) {
        return ResponseEntity.ok(bookingService.addEmergencySlot(dto));
    }
}
