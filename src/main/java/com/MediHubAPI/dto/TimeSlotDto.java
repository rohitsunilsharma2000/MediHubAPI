package com.MediHubAPI.dto;

import com.MediHubAPI.model.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    private String time;
    private SlotStatus status;
    private String patientName;
}
