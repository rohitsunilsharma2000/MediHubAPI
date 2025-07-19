package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class PatientDetailsDto {
    private String bloodGroup;
    private String fatherName;
    private String motherName;
    private String spouseName;
    private String alternateContact;
    private String education;
    private String occupation;
    private String religion;
    private String ivrLanguage;
    private Double birthWeight;
}