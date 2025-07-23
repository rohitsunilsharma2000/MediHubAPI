package com.MediHubAPI.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalkInAppointmentDto {
    @NotNull
    private Long doctorId;
    @NotNull
    private Long patientId;
    @NotNull
    private LocalDate date;
    @NotNull
    private LocalTime time;
}
