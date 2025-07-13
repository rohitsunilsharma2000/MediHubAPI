package com.MediHubAPI.dto.availability;

import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.model.enums.SlotType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AdditionalSlotDTO {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Slot status is required")
    private SlotStatus status;

    @NotNull(message = "Slot type is required")
    private SlotType slotType;

    @NotBlank(message = "Added by is required")
    private String addedBy;

    private String reason;

    private String priorityTag;
}
