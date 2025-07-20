package com.MediHubAPI.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SlotResponseDto {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
}