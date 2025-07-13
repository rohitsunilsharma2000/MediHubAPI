package com.MediHubAPI.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AppointmentArriveDTO {

    @NotNull(message = "Slot ID is required")
    private Long slotId;
}
