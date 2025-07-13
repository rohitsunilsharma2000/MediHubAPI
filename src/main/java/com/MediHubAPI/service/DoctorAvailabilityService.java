package com.MediHubAPI.service;

import com.MediHubAPI.dto.availability.*;
import com.MediHubAPI.model.*;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.model.enums.SlotType;
import com.MediHubAPI.repository.*;
import com.MediHubAPI.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepo;
    private final AppointmentSlotRepository slotRepo;
    private final UserRepository userRepo;

    @Transactional
    public String createAvailabilityWithSlots(DoctorAvailabilityRequestDTO dto) {
        User doctor = userRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));

        if (availabilityRepo.existsByDoctorAndDate(doctor, dto.getDate())) {
            throw new ValidationException("Availability already exists for this doctor and date");
        }

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .slotDurationInMinutes(dto.getSlotDurationInMinutes())
                .build();

        List<AppointmentSlot> slots = generateSlots(availability);
        availability.setSlots(slots);

        availabilityRepo.save(availability);
        return "Doctor availability and slots created successfully.";
    }

    private List<AppointmentSlot> generateSlots(DoctorAvailability availability) {
        List<AppointmentSlot> slots = new ArrayList<>();
        LocalTime current = availability.getStartTime();
        while (current.plusMinutes(availability.getSlotDurationInMinutes()).compareTo(availability.getEndTime()) <= 0) {
            LocalTime end = current.plusMinutes(availability.getSlotDurationInMinutes());

            AppointmentSlot slot = AppointmentSlot.builder()
                    .date(availability.getDate())
                    .startTime(current)
                    .endTime(end)
                    .status(SlotStatus.AVAILABLE)
                    .slotType(SlotType.NORMAL)
                    .doctorAvailability(availability)
                    .build();

            slots.add(slot);
            current = end;
        }
        return slots;
    }

    public List<AppointmentSlot> getSlotsByDoctorAndDate(Long doctorId, java.time.LocalDate date) {
        User doctor = userRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        List<DoctorAvailability> availabilities = availabilityRepo.findByDoctorAndDate(doctor, date);
        List<AppointmentSlot> allSlots = new ArrayList<>();
        for (DoctorAvailability availability : availabilities) {
            allSlots.addAll(slotRepo.findByDoctorAvailability(availability));
        }
        return allSlots;
    }

    public String addAdditionalSlot(AdditionalSlotDTO dto) {
        User doctor = userRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        DoctorAvailability availability = availabilityRepo.findByDoctorAndDate(doctor, dto.getDate())
                .stream().findFirst()
                .orElseThrow(() -> new ValidationException("Doctor availability not found for the date"));

        if (slotRepo.existsByDoctorAvailabilityAndStartTimeAndEndTime(availability, dto.getStartTime(), dto.getEndTime())) {
            throw new ValidationException("Slot with given time already exists");
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
        return "Additional slot created successfully.";
    }

    public String blockSlot(BlockSlotRequestDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        slot.setStatus(SlotStatus.BLOCKED);
        slot.setReason(dto.getReason());
        slot.setAddedBy(dto.getAddedBy());

        slotRepo.save(slot);
        return "Slot successfully blocked.";
    }

    public String updateSlotStatus(SlotStatusUpdateDTO dto) {
        AppointmentSlot slot = slotRepo.findById(dto.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        slot.setStatus(dto.getNewStatus());
        slotRepo.save(slot);

        return "Slot status updated to " + dto.getNewStatus();
    }
}
