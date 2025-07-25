package com.MediHubAPI.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
public class DoctorAvailabilityDto {
    private Integer slotDurationInMinutes;

    // Existing: Recurring weekly availability (e.g., every Monday)
    private Map<DayOfWeek, List<TimeRange>> weeklyAvailability;

    // 🔥 New: Date-specific availability (e.g., 2025-07-28 only)
    private Map<LocalDate, List<TimeRange>> dateWiseAvailability;

    @Data
    public static class TimeRange {
        @NotNull
        private LocalTime start;

        @NotNull
        private LocalTime end;
    }
}
