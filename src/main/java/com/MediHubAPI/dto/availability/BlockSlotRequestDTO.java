package com.MediHubAPI.dto.availability;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BlockSlotRequestDTO {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Added by is required")
    private String addedBy;
}
