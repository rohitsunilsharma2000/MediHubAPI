package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class PatientAddressDto {
    private String address;
    private String area;
    private String city;
    private String pinCode;
    private String state;
    private String country;
    private String nationality;
    private Boolean internationalPatient;
}