package com.MediHubAPI.controller;

import com.MediHubAPI.dto.PatientCreateDto;
import com.MediHubAPI.dto.PatientResponseDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.service.PatientService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ✅ 1. Register a new patient
    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody PatientCreateDto patientDto) {
        try {
            log.info("Registering new patient: {}", patientDto.getFirstName());
            PatientResponseDto response = patientService.registerPatient(patientDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (HospitalAPIException e) {
            log.error("Error registering patient: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error registering patient", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register patient");
        }
    }

    // ✅ 2. Search by name, phone, ID, father/mother name, DOB, or hospital ID
//    @GetMapping("/search")
//    public ResponseEntity<?> searchPatients(@RequestParam(required = false) String name,
//                                            @RequestParam(required = false) String phone,
//                                            @RequestParam(required = false) String fileNumber,
//                                            @RequestParam(required = false) String fatherName,
//                                            @RequestParam(required = false) String motherName,
//                                            @RequestParam(required = false) String dob,
//                                            @RequestParam(required = false) String hospitalId) {
//        try {
//            log.info("Searching patients with criteria: name={}, phone={}, fileNumber={}, fatherName={}, motherName={}, dob={}, hospitalId={}",
//                    name, phone, fileNumber, fatherName, motherName, dob, hospitalId);
//
//            List<PatientResponseDto> results = patientService.searchPatients(name, phone, fileNumber, fatherName, motherName, dob, hospitalId);
//            return ResponseEntity.ok(results);
//        } catch (Exception e) {
//            log.error("Error while searching patients", e);
//            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search patients");
//        }
//    }
    @GetMapping("/search")
    public ResponseEntity<?> searchPatients(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String fileNumber,
                                            @RequestParam(required = false) String fatherName,
                                            @RequestParam(required = false) String motherName,
                                            @RequestParam(required = false) String dob,
                                            @RequestParam(required = false) String hospitalId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Searching patients with filters");
            Pageable pageable = PageRequest.of(page, size);
            Page<PatientResponseDto> results = patientService.searchPatients(
                    name, phone, fileNumber, fatherName, motherName, dob, hospitalId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error while searching patients", e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search patients");
        }
    }

    // ✅ 3. Get patient by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            log.info("Fetching patient details by ID: {}", id);
            return ResponseEntity.ok(patientService.getPatientById(id));
        } catch (Exception e) {
            log.error("Error fetching patient with ID {}", id, e);
            throw new HospitalAPIException(HttpStatus.NOT_FOUND, "Patient not found");
        }
    }

    // ✅ 4. Update patient by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientCreateDto updateDto) {
        try {
            log.info("Updating patient with ID: {}", id);
            PatientResponseDto updated = patientService.updatePatient(id, updateDto);
            return ResponseEntity.ok(updated);
        } catch (HospitalAPIException e) {
            log.error("Domain error while updating patient {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating patient with ID {}", id, e);
            throw new HospitalAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update patient");
        }
    }
}
