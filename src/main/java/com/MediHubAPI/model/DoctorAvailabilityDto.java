package com.MediHubAPI.model;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
public class DoctorAvailabilityDto {
    private Integer slotDurationInMinutes;
    private Map<DayOfWeek, List<TimeRange>> weeklyAvailability;

    @Data
    public static class TimeRange {
        private LocalTime start;
        private LocalTime end;
    }
}
