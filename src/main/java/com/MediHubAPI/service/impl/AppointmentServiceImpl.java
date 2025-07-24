package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.Slot;
import com.MediHubAPI.model.User;
import com.MediHubAPI.model.enums.AppointmentStatus;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.repository.AppointmentRepository;
import com.MediHubAPI.repository.UserRepository;
import com.MediHubAPI.service.AppointmentService;
import com.MediHubAPI.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotService slotService;  // ✅ Injected

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
    public Page<AppointmentResponseDto> getAppointmentsWithFilters(LocalDate date, String doctorName, AppointmentStatus status, String range, Pageable pageable) {
        LocalDate finalDate = resolveDateRange(date, range);

        return appointmentRepo.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();

            if (finalDate != null) {
                predicates = cb.and(predicates, cb.equal(root.get("appointmentDate"), finalDate));
            }

            if (doctorName != null && !doctorName.isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("doctor").get("firstName")), "%" + doctorName.toLowerCase() + "%"));
            }

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            return predicates;
        }, pageable).map(a -> modelMapper.map(a, AppointmentResponseDto.class));
    }

    private LocalDate resolveDateRange(LocalDate date, String range) {
        if (date != null) return date;
        LocalDate today = LocalDate.now();

        return switch (range != null ? range.toUpperCase() : "") {
            case "TODAY" -> today;
            case "DAY" -> today.plusDays(1);
            case "PREV" -> today.minusDays(1);
            case "NEXT" -> today.plusDays(1);
            case "WEEK" -> null;  // You can implement WEEK logic using BETWEEN query instead
            case "MONTH" -> null; // Implement if required
            default -> null;
        };
    }

    @Override
    public void markAsArrived(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new HospitalAPIException(HttpStatus.BAD_REQUEST, "Only booked appointments can be marked as arrived");
        }

        appointment.setStatus(AppointmentStatus.ARRIVED);
        appointmentRepo.save(appointment);
        log.info("✅ Appointment {} marked as ARRIVED", appointmentId);
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


    @Override
    public Page<DoctorScheduleDto> getDoctorSchedulesStructured(LocalDate date, String doctorName, String specialization, Pageable pageable) {
        List<User> doctors = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().name().equals("DOCTOR")))
                .filter(u -> doctorName == null || (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(doctorName.toLowerCase()))
                .filter(u -> specialization == null || (u.getSpecialization() != null &&
                        u.getSpecialization().getName().equalsIgnoreCase(specialization)))
                .toList();

        List<DoctorScheduleDto> dtos = doctors.stream().map(doctor -> {
            List<Slot> slots = slotService.getEmergencySlots(doctor.getId(), date); // or a new method like getAllSlotsForDoctorDate
            List<HourlySlotGroupDto> grouped = groupSlotsByHour(slots);

            return DoctorScheduleDto.builder()
                    .id(doctor.getId())
                    .name("Dr. " + doctor.getFirstName() + " " + doctor.getLastName())
                    .specialization(doctor.getSpecialization() != null ? doctor.getSpecialization().getName() : "General")
                    .avatarUrl("https://storage.googleapis.com/uxpilot-auth.appspot.com/avatars/avatar-" + (doctor.getId() % 6 + 1) + ".jpg")
                    .timeSlots(grouped)
                    .build();
        }).toList();

        return new PageImpl<>(dtos, pageable, dtos.size());
    }
    private List<HourlySlotGroupDto> groupSlotsByHour(List<Slot> slots) {
        // Group slots into 10-min segments by base hour
        Map<String, List<TimeSlotDto>> hourlyGrouped = new TreeMap<>();

        for (Slot slot : slots) {
            String hourLabel = slot.getStartTime().withMinute(0).toString(); // "09:00"
            String exactTime = slot.getStartTime().toString();              // "09:10"

            TimeSlotDto dto = TimeSlotDto.builder()
                    .time(exactTime)
                    .status(slot.getStatus())
                    .patientName(
                            slot.getAppointment() != null && slot.getAppointment().getPatient() != null
                                    ? slot.getAppointment().getPatient().getFirstName() + " " + slot.getAppointment().getPatient().getLastName()
                                    : (slot.getStatus() == SlotStatus.LUNCH_BREAK ? "Lunch Break" : null)
                    )
                    .build();

            hourlyGrouped.computeIfAbsent(hourLabel, k -> new ArrayList<>()).add(dto);
        }

        return hourlyGrouped.entrySet().stream()
                .map(e -> HourlySlotGroupDto.builder()
                        .timeLabel(e.getKey())
                        .slots(e.getValue().stream()
                                .sorted(Comparator.comparing(TimeSlotDto::getTime))
                                .toList())
                        .build())
                .toList();
    }


}
