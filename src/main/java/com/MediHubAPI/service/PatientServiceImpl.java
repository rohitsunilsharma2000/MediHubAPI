package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.PatientCreateDto;
import com.MediHubAPI.dto.PatientResponseDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.Patient;
import com.MediHubAPI.repository.PatientRepository;
import com.MediHubAPI.service.PatientService;
import com.MediHubAPI.specification.PatientSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper mapper;

    @Override
    public PatientResponseDto registerPatient(PatientCreateDto dto) {
        Patient patient = mapper.map(dto, Patient.class);

        // ⚠️ Manually set bi-directional references
        if (patient.getReferrer() != null) patient.getReferrer().setPatient(patient);
        if (patient.getAddress() != null) patient.getAddress().setPatient(patient);
        if (patient.getPhoto() != null) patient.getPhoto().setPatient(patient);
        if (patient.getDetails() != null) patient.getDetails().setPatient(patient);
        if (patient.getNotes() != null) patient.getNotes().setPatient(patient);

        // Save patient with cascade = ALL
        Patient saved = patientRepository.save(patient);
        return mapper.map(saved, PatientResponseDto.class);
    }

    @Override
    public PatientResponseDto getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Patient not found"));
        return mapper.map(patient, PatientResponseDto.class);
    }

    @Override
    public PatientResponseDto updatePatient(Long id, PatientCreateDto dto) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Patient not found"));
        mapper.map(dto, existing);
        Patient updated = patientRepository.save(existing);
        return mapper.map(updated, PatientResponseDto.class);
    }

    @Override
    public Page<PatientResponseDto> searchPatients(String name, String phone, String fileNumber,
                                                   String fatherName, String motherName, String dob,
                                                   String hospitalId, Pageable pageable) {
        Specification<Patient> spec = Specification
                .where(PatientSpecification.contains("firstName", name))
                .or(PatientSpecification.contains("lastName", name))
                .or(PatientSpecification.contains("mobileNumber", phone))
                .or(PatientSpecification.contains("fileNumber", fileNumber))
                .or(PatientSpecification.contains("fatherName", fatherName))
                .or(PatientSpecification.contains("motherName", motherName))
                .or(PatientSpecification.contains("dateOfBirth", dob))
                .or(PatientSpecification.contains("hospitalId", hospitalId));

        Page<Patient> page = patientRepository.findAll(spec, pageable);
        return page.map(patient -> mapper.map(patient, PatientResponseDto.class));
    }
}
