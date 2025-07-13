package com.MediHubAPI.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AppointmentBookingRequestDTO {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotBlank(message = "Payment status is required")
    private String paymentStatus; // e.g., Paid, Unpaid
}
