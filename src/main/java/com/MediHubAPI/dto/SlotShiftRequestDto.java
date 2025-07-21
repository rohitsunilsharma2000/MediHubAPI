package com.MediHubAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SlotShiftRequestDto {
    @NotNull
    private LocalDate date;
    @NotNull private Integer shiftByMinutes;
}

