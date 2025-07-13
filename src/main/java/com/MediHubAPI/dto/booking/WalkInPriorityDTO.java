package com.MediHubAPI.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WalkInPriorityDTO {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotBlank(message = "Priority tag is required")
    private String priorityTag;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;
}
