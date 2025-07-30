package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.Slot;
import com.MediHubAPI.model.enums.AppointmentStatus;
import com.MediHubAPI.model.enums.AppointmentType;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.repository.AppointmentRepository;
import com.MediHubAPI.repository.SlotRepository;
import com.MediHubAPI.repository.UserRepository;
import com.MediHubAPI.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void shiftSlots(Long doctorId, SlotShiftRequestDto request) {
        List<Slot> slots = slotRepository.findByDoctorIdAndDate(doctorId, request.getDate());
        for (Slot slot : slots) {
            slot.setStartTime(slot.getStartTime().plusMinutes(request.getShiftByMinutes()));
            slot.setEndTime(slot.getEndTime().plusMinutes(request.getShiftByMinutes()));
        }
        slotRepository.saveAll(slots);
        log.info("Doctor {} slots shifted by {} mins", doctorId, request.getShiftByMinutes());
    }

    @Transactional
    public void blockSlots(Long doctorId, SlotBlockRequestDto request) {
        List<Slot> slots = slotRepository.findByDoctorIdAndDateAndStartTimeBetween(
                doctorId, request.getDate(), request.getStartTime(), request.getEndTime());

        for (Slot slot : slots) {
            if (request.isCancelExisting() && slot.getAppointment() != null) {
                Appointment appointment = slot.getAppointment();
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);
            }
            slot.setStatus(SlotStatus.BLOCKED);
        }
        slotRepository.saveAll(slots);
        log.info("Doctor {} blocked {} slots", doctorId, slots.size());
    }

    @Transactional
    public void unblockSlots(Long doctorId, SlotUnblockRequestDto request) {
        List<Slot> slots = slotRepository.findByDoctorIdAndDateAndStartTimeBetween(
                doctorId, request.getDate(), request.getStartTime(), request.getEndTime());

        for (Slot slot : slots) {
            if (slot.getStatus() == SlotStatus.BLOCKED) {
                slot.setStatus(SlotStatus.AVAILABLE);
            }
        }
        slotRepository.saveAll(slots);
        log.info("Doctor {} unblocked {} slots", doctorId, slots.size());
    }

    public List<SlotStatusDto> getSlotStatuses(Long doctorId, LocalDate date) {
        List<Slot> slots = slotRepository.findByDoctorIdAndDate(doctorId, date);
        return slots.stream().map(slot -> SlotStatusDto.builder()
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus().name())
                .type(slot.getType().name())
                .color(slot.getStatus().getColorCode())
                .build()).toList();
    }

    @Transactional
    public Appointment bookWalkInSlot(WalkInAppointmentDto dto) {
        List<Slot> slots = slotRepository.findByDoctorIdAndDateAndStatusIn(
                dto.getDoctorId(), dto.getDate(), List.of(SlotStatus.WALKIN, SlotStatus.AVAILABLE));

        Optional<Slot> available = slots.stream().filter(s -> s.getStartTime().equals(dto.getTime())).findFirst();

        if (available.isEmpty()) {
            throw new HospitalAPIException(HttpStatus.NOT_FOUND, "No walk-in slot available at given time");
        }

        Slot slot = available.get();
        slot.setStatus(SlotStatus.BOOKED);

        Appointment appointment = new Appointment();
        appointment.setDoctor(userRepository.findById(dto.getDoctorId()).orElseThrow());
        appointment.setPatient(userRepository.findById(dto.getPatientId()).orElseThrow());
        appointment.setSlot(slot);
        appointment.setDate(dto.getDate());
        appointment.setType(AppointmentType.WALKIN);
        appointment.setStatus(AppointmentStatus.BOOKED);

        slot.setAppointment(appointment);
        slotRepository.save(slot);
        return appointmentRepository.save(appointment);
    }

    public List<Slot> getEmergencySlots(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndDateAndStatusIn(
                doctorId, date, List.of(SlotStatus.WALKIN, SlotStatus.AVAILABLE));
    }

    @Override
    public Slot getSlotByDoctorAndTime(Long doctorId, LocalDate appointmentDate, LocalTime slotTime) {
        log.debug("🔍 Fetching slot for doctorId={}, date={}, time={}", doctorId, appointmentDate, slotTime);

        return slotRepository.findByDoctorIdAndStartTimeAndDate(doctorId, slotTime, appointmentDate)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "No slot found at this time for the doctor"));

    }


}

