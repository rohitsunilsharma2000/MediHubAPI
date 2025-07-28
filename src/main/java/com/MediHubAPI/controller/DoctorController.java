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
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            DoctorSearchCriteria criteria) {
        try {
            log.info("🔍 Fetching doctors with criteria={} page={} size={}", criteria, page, size);
            Page<UserDto> result = doctorService.searchDoctors(criteria, PageRequest.of(page, size));
            return ResponseEntity.ok(ApiResponse.success(result, "/doctors", "Doctors fetched successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to fetch doctors", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch doctors");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorProfileDto>> getDoctorProfile(@PathVariable Long id) {
        log.info("👨‍⚕️ Fetching doctor profile for ID={}", id);
        DoctorProfileDto dto = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "/doctors/" + id, "Doctor profile fetched successfully"));
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> defineAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityDto dto) {
        log.info("🗓️ Defining availability for doctor ID={} with data={}", id, dto);
        doctorService.defineAvailability(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Availability defined successfully", "/doctors/" + id + "/availability"));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityDto dto) {
        log.info("🔄 Updating availability for doctor ID={} with data={}", id, dto);
        doctorService.updateAvailability(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Availability updated successfully", "/doctors/" + id + "/availability"));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<ApiResponse<List<SlotResponseDto>>> getSlotsByDate(
            @PathVariable Long id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("📅 Fetching slots for doctor ID={} on date={}", id, date);
        List<SlotResponseDto> slots = doctorService.getSlotsForDate(id, date);
        return ResponseEntity.ok(ApiResponse.success(slots, "/doctors/" + id + "/slots?date=" + date, "Slots fetched successfully"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateDoctor(@PathVariable Long id) {
        log.info("🚫 Deactivating doctor with ID={}", id);
        doctorService.deactivateDoctor(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor deactivated successfully", "/doctors/" + id + "/deactivate"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        log.info("🗑️ Deleting doctor with ID={}", id);
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor deleted successfully", "/doctors/" + id));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchDoctorsByKeyword(
            @RequestParam String keyword) {
        try {
            log.info("🔍 Searching doctors by keyword: {}", keyword);

            // simulate delay for UX like Google Search
            Thread.sleep(300);

            List<UserDto> results = doctorService.searchDoctorsByKeyword(keyword);
            return ResponseEntity.ok(ApiResponse.success(results, "/doctors/search", "Doctors search successful"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Search interrupted");
        } catch (Exception e) {
            log.error("❌ Failed to search doctors", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search doctors");
        }
    }

}
