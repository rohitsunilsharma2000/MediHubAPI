package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class DoctorSearchCriteria {
    private String name;
    private String email;
    private Boolean active;
}

