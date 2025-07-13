package com.MediHubAPI.controller;

import com.MediHubAPI.dto.availability.*;
import com.MediHubAPI.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    // Create availability and auto-generate slots
    @PostMapping
    public ResponseEntity<?> createAvailability(@Valid @RequestBody DoctorAvailabilityRequestDTO dto) {
        return ResponseEntity.ok(availabilityService.createAvailabilityWithSlots(dto));
    }

    // Get all slots for a doctor by date
    @GetMapping("/slots")
    public ResponseEntity<?> getSlotsByDoctorAndDate(
            @RequestParam Long doctorId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(availabilityService.getSlotsByDoctorAndDate(doctorId, date));
    }

    // Add additional (manual) slot
    @PostMapping("/additional-slot")
    public ResponseEntity<?> addAdditionalSlot(@Valid @RequestBody AdditionalSlotDTO dto) {
        return ResponseEntity.ok(availabilityService.addAdditionalSlot(dto));
    }

    // Block a specific slot
    @PostMapping("/block-slot")
    public ResponseEntity<?> blockSlot(@Valid @RequestBody BlockSlotRequestDTO dto) {
        return ResponseEntity.ok(availabilityService.blockSlot(dto));
    }

    // Update slot status (e.g., to ARRIVED, COMPLETED)
    @PutMapping("/update-status")
    public ResponseEntity<?> updateSlotStatus(@Valid @RequestBody SlotStatusUpdateDTO dto) {
        return ResponseEntity.ok(availabilityService.updateSlotStatus(dto));
    }
}
