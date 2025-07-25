package com.MediHubAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlySlotGroupDto {
    private String timeLabel; // e.g., "09:00"
    private List<TimeSlotDto> slots;
}
