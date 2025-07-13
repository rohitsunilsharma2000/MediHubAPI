package com.MediHubAPI.service;

import com.MediHubAPI.dto.availability.AdditionalSlotDTO;
import com.MediHubAPI.dto.booking.*;
import com.MediHubAPI.exception.ResourceNotFoundException;
import com.MediHubAPI.exception.SlotNotFoundException;
import com.MediHubAPI.exception.ValidationException;
import com.MediHubAPI.model.AppointmentSlot;
import com.MediHubAPI.model.DoctorAvailability;
import com.MediHubAPI.model.User;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.model.enums.SlotType;
import com.MediHubAPI.repository.AppointmentSlotRepository;
import com.MediHubAPI.repository.DoctorAvailabilityRepository;
import com.MediHubAPI.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    private final AppointmentSlotRepository slotRepo;
    private final UserRepository userRepo;
    private final DoctorAvailabilityRepository availabilityRepo;

    public List<AppointmentSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        return slotRepo.findByDoctorAvailabilityDateAndDoctorAvailabilityDoctorIdAndStatus(
                date, doctorId, SlotStatus.AVAILABLE);
    }

    @Transactional
    public String bookAppointment(AppointmentBookingRequestDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ValidationException("Slot is not available for booking");
        }

        if (slot.getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot book past date slot");
        }

        User patient = userRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        slot.setStatus(SlotStatus.BOOKED);
        slot.setPatient(patient);

        slotRepo.save(slot);
        return "Appointment booked successfully.";
    }

    @Transactional
    public String cancelAppointment(AppointmentCancelDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.BOOKED) {
            throw new ValidationException("Only booked slots can be cancelled");
        }

        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setPatient(null);
        slotRepo.save(slot);
        return "Appointment cancelled successfully.";
    }

    @Transactional
    public String markArrived(AppointmentArriveDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.BOOKED) {
            throw new ValidationException("Only booked slots can be marked as arrived");
        }

        slot.setStatus(SlotStatus.ARRIVED);
        slotRepo.save(slot);
        return "Appointment marked as ARRIVED.";
    }

    @Transactional
    public String markCompleted(AppointmentCompleteDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.ARRIVED) {
            throw new ValidationException("Only ARRIVED slots can be marked as completed");
        }

        slot.setStatus(SlotStatus.COMPLETED);
        slotRepo.save(slot);
        return "Appointment marked as COMPLETED.";
    }

    @Transactional
    public String markNoShow(AppointmentNoShowDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.BOOKED && slot.getStatus() != SlotStatus.ARRIVED) {
            throw new ValidationException("Slot must be booked or arrived to mark no-show");
        }

        slot.setStatus(SlotStatus.NO_SHOW);
        slotRepo.save(slot);
        return "Appointment marked as NO_SHOW.";
    }

    @Transactional
    public String assignWalkInPriority(WalkInPriorityDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        slot.setPriorityTag(dto.getPriorityTag());
        slot.setReason(dto.getReason());
        slotRepo.save(slot);
        return "Walk-in priority assigned successfully.";
    }

    @Transactional
    public String addEmergencySlot(AdditionalSlotDTO dto) {
        User doctor = userRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        DoctorAvailability availability = availabilityRepo.findByDoctorAndDate(doctor, dto.getDate())
                .stream().findFirst()
                .orElseThrow(() -> new ValidationException("Doctor availability not found"));

        boolean exists = slotRepo.existsByDoctorAvailabilityAndStartTimeAndEndTime(
                availability, dto.getStartTime(), dto.getEndTime());

        if (exists) {
            throw new ValidationException("Slot already exists for the given time");
        }

        AppointmentSlot slot = AppointmentSlot.builder()
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(dto.getStatus())
                .slotType(dto.getSlotType())
                .priorityTag(dto.getPriorityTag())
                .addedBy(dto.getAddedBy())
                .reason(dto.getReason())
                .doctorAvailability(availability)
                .build();

        slotRepo.save(slot);
        return "Emergency slot added successfully.";
    }
}
