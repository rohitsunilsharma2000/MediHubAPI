package com.MediHubAPI.dto.availability;

import com.MediHubAPI.model.enums.SlotStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SlotStatusUpdateDTO {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "New status is required")
    private SlotStatus newStatus;
}
