package com.MediHubAPI.service;

import com.MediHubAPI.dto.PatientCreateDto;
import com.MediHubAPI.dto.PatientResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {
    PatientResponseDto registerPatient(PatientCreateDto dto);

    PatientResponseDto getPatientById(Long id);

    PatientResponseDto updatePatient(Long id, PatientCreateDto dto);

    Page<PatientResponseDto> searchPatients(String name, String phone, String fileNumber,
                                            String fatherName, String motherName, String dob,
                                            String hospitalId, Pageable pageable);
}
