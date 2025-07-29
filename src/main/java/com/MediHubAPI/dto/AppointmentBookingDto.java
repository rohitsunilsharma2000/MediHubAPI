package com.MediHubAPI.dto;


import com.MediHubAPI.model.enums.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Builder
@Data
public class AppointmentBookingDto {
    @NotNull private Long doctorId;
    @NotNull private Long patientId;
    @NotNull private LocalDate appointmentDate;
    @NotNull private LocalTime slotTime;
    @NotNull private AppointmentType appointmentType;
}

