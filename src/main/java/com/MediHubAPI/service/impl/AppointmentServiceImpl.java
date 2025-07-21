package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.AppointmentBookingDto;
import com.MediHubAPI.dto.AppointmentResponseDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.*;
import com.MediHubAPI.model.enums.AppointmentStatus;
import com.MediHubAPI.repository.AppointmentRepository;
import com.MediHubAPI.repository.UserRepository;
import com.MediHubAPI.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    @Override
    public AppointmentResponseDto bookAppointment(AppointmentBookingDto dto) {
        User doctor = userRepo.findById(dto.getDoctorId()).orElseThrow(() ->
                new HospitalAPIException(HttpStatus.NOT_FOUND, "Doctor not found"));

        User patient = userRepo.findById(dto.getPatientId()).orElseThrow(() ->
                new HospitalAPIException(HttpStatus.NOT_FOUND, "Patient not found"));

        if (appointmentRepo.existsByDoctorAndAppointmentDateAndSlotTime(doctor, dto.getAppointmentDate(), dto.getSlotTime())) {
            throw new HospitalAPIException(HttpStatus.CONFLICT, "Doctor already has appointment in this slot");
        }

        if (appointmentRepo.existsByPatientAndAppointmentDateAndSlotTime(patient, dto.getAppointmentDate(), dto.getSlotTime())) {
            throw new HospitalAPIException(HttpStatus.CONFLICT, "Patient already has appointment in this slot");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setSlotTime(dto.getSlotTime());
        appointment.setType(dto.getAppointmentType());
        appointment.setStatus(AppointmentStatus.BOOKED);
        Appointment saved = appointmentRepo.save(appointment);
        return modelMapper.map(saved, AppointmentResponseDto.class);
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsForDoctor(Long doctorId, LocalDate date) {
        return appointmentRepo.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
                .map(a -> modelMapper.map(a, AppointmentResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsForPatient(Long patientId) {
        return appointmentRepo.findByPatientIdOrderByAppointmentDateDesc(patientId).stream()
                .map(a -> modelMapper.map(a, AppointmentResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new HospitalAPIException(HttpStatus.BAD_REQUEST, "Already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appointment);
    }

    @Override
    public AppointmentResponseDto reschedule(Long id, AppointmentBookingDto dto) {
        Appointment existing = appointmentRepo.findById(id)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Appointment not found"));

        cancelAppointment(id);

        AppointmentBookingDto newDto = new AppointmentBookingDto();
        newDto.setDoctorId(dto.getDoctorId());
        newDto.setPatientId(dto.getPatientId());
        newDto.setAppointmentDate(dto.getAppointmentDate());
        newDto.setSlotTime(dto.getSlotTime());
        newDto.setAppointmentType(dto.getAppointmentType());

        return bookAppointment(newDto);
    }
}
