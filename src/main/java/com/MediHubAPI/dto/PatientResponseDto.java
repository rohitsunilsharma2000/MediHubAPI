package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class PatientResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String fileNumber;
    private String fatherName;
    private String motherName;
    private String hospitalId;
    private String dateOfBirth;
    private String email;
    private String gender;
}
