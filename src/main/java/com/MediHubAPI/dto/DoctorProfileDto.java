package com.MediHubAPI.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorProfileDto {
    private Long id;
    private String name;
    private String email;
    private boolean enabled;
    private List<String> specializations;
}
