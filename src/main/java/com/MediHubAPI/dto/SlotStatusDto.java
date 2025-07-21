package com.MediHubAPI.dto;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlotStatusDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String type;
    private String color;
}
