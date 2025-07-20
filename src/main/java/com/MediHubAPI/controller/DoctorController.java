package com.MediHubAPI.controller;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Slf4j
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<?> getAllDoctors(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           DoctorSearchCriteria criteria) {
        try {
            Page<UserDto> result = doctorService.searchDoctors(criteria, PageRequest.of(page, size));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch doctors", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch doctors");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<?> defineAvailability(@PathVariable Long id,
                                                @Valid @RequestBody DoctorAvailabilityDto dto) {
        doctorService.defineAvailability(id, dto);
        return ResponseEntity.ok("Availability defined successfully.");
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long id,
                                                @Valid @RequestBody DoctorAvailabilityDto dto) {
        doctorService.updateAvailability(id, dto);
        return ResponseEntity.ok("Availability updated successfully.");
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getSlotsByDate(@PathVariable Long id,
                                            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorService.getSlotsForDate(id, date));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateDoctor(@PathVariable Long id) {
        doctorService.deactivateDoctor(id);
        return ResponseEntity.ok("Doctor deactivated.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok("Doctor deleted.");
    }
}
