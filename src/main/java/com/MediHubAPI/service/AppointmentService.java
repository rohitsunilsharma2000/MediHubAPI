package com.MediHubAPI.service;

import com.MediHubAPI.dto.AppointmentBookingDto;
import com.MediHubAPI.dto.AppointmentResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentResponseDto bookAppointment(AppointmentBookingDto dto);
    List<AppointmentResponseDto> getAppointmentsForDoctor(Long doctorId, LocalDate date);
    List<AppointmentResponseDto> getAppointmentsForPatient(Long patientId);
    void cancelAppointment(Long appointmentId);
    AppointmentResponseDto reschedule(Long id, AppointmentBookingDto dto);
}
