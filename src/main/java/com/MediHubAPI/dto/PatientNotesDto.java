package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class PatientNotesDto {
    private Boolean needsAttention;
    private String notes;
}
