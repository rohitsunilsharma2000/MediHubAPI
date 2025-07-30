package com.MediHubAPI.dto;

import com.MediHubAPI.model.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotInfoDto {
    private Long slotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
}
