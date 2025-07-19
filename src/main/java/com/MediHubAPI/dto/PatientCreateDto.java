package com.MediHubAPI.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PatientCreateDto {
    private String title;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String landlineNumber;
    private String fileNumber;
//    private LocalDate dateOfBirth;


    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
    private String dateOfBirth;

    private Integer ageYears;
    private Integer ageMonths;
    private Integer ageDays;
    private String sex; // Or use Enum Gender
    private String maritalStatus; // Or use Enum MaritalStatus
    private String motherTongue;
    private String govtIdType;
    private String govtIdNumber;
    private String otherHospitalIds;
    private String email;



    // ✅ Nested DTOs (must match JSON structure)
    private ReferrerDto referrer;
    private PatientAddressDto address;
    private PatientPhotoDto photo;
    private PatientDetailsDto details;
    private PatientNotesDto notes;
}