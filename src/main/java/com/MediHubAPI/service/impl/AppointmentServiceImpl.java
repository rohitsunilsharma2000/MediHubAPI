package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.AppointmentBookingDto;
import com.MediHubAPI.dto.AppointmentResponseDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.User;
import com.MediHubAPI.model.enums.AppointmentStatus;
import com.MediHubAPI.repository.AppointmentRepository;
import com.MediHubAPI.repository.UserRepository;
import com.MediHubAPI.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        log.debug("🔁 Booking appointment: {}", dto);

        User doctor = findUserOrThrow(dto.getDoctorId(), "Doctor");
        User patient = findUserOrThrow(dto.getPatientId(), "Patient");

        // Check for slot conflicts
        if (appointmentRepo.existsByDoctorAndAppointmentDateAndSlotTime(doctor, dto.getAppointmentDate(), dto.getSlotTime())) {
            throw new HospitalAPIException(HttpStatus.CONFLICT, "Doctor already has an appointment in this slot");
        }

        if (appointmentRepo.existsByPatientAndAppointmentDateAndSlotTime(patient, dto.getAppointmentDate(), dto.getSlotTime())) {
            throw new HospitalAPIException(HttpStatus.CONFLICT, "Patient already has an appointment in this slot");
        }

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(dto.getAppointmentDate())
                .slotTime(dto.getSlotTime())
                .type(dto.getAppointmentType())
                .status(AppointmentStatus.BOOKED)
                .build();

        Appointment saved = appointmentRepo.save(appointment);
        log.info("✅ Appointment booked successfully: {}", saved.getId());
        return modelMapper.map(saved, AppointmentResponseDto.class);
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsForDoctor(Long doctorId, LocalDate date) {
        log.debug("📄 Fetching appointments for doctorId={} on {}", doctorId, date);
        return appointmentRepo.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsForPatient(Long patientId) {
        log.debug("📄 Fetching appointments for patientId={}", patientId);
        return appointmentRepo.findByPatientIdOrderByAppointmentDateDesc(patientId).stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<AppointmentResponseDto> getAppointmentsForPatient(Long patientId, Pageable pageable) {
        log.debug("📄 Fetching paginated appointments for patientId={} with pageable={}", patientId, pageable);
        return appointmentRepo.findByPatientId(patientId, pageable)
                .map(appointment -> modelMapper.map(appointment, AppointmentResponseDto.class));
    }

    @Override
    public void cancelAppointment(Long appointmentId) {
        log.info("🗑️ Cancelling appointment with id={}", appointmentId);
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (AppointmentStatus.CANCELLED.equals(appointment.getStatus())) {
            throw new HospitalAPIException(HttpStatus.BAD_REQUEST, "Appointment already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appointment);
        log.info("✅ Appointment cancelled: {}", appointmentId);
    }

    @Override
    public AppointmentResponseDto reschedule(Long id, AppointmentBookingDto dto) {
        log.info("♻️ Rescheduling appointment with id={}", id);
        Appointment existing = appointmentRepo.findById(id)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Appointment not found"));

        // Mark existing as cancelled
        cancelAppointment(id);

        // Reuse existing booking logic
        return bookAppointment(dto);
    }

    // 🔁 Utility method for finding users (doctor or patient)
    private User findUserOrThrow(Long userId, String type) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, type + " not found"));
    }
}
