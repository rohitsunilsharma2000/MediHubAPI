package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.dto.DoctorAvailabilityDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.exception.ResourceNotFoundException;
import com.MediHubAPI.model.*;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.model.enums.SlotType;
import com.MediHubAPI.repository.SlotRepository;
import com.MediHubAPI.repository.UserRepository;
import com.MediHubAPI.service.DoctorService;
import com.MediHubAPI.specification.DoctorSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<UserDto> searchDoctors(DoctorSearchCriteria criteria, Pageable pageable) {
        Specification<User> spec = new DoctorSpecification(criteria);
        return userRepository.findAll(spec, pageable)
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    @Override
    public DoctorProfileDto getDoctorById(Long id) {
        User doctor = validateDoctor(id);
        return modelMapper.map(doctor, DoctorProfileDto.class);
    }

    @Override
    public void defineAvailability(Long doctorId, DoctorAvailabilityDto dto) {
        User doctor = validateDoctor(doctorId);

        int duration = dto.getSlotDurationInMinutes();
        Map<DayOfWeek, List<DoctorAvailabilityDto.TimeRange>> availabilityMap = dto.getWeeklyAvailability();

        availabilityMap.forEach((day, timeRanges) -> {
            for (DoctorAvailabilityDto.TimeRange range : timeRanges) {
                for (int week = 0; week < 4; week++) {
                    LocalDate targetDate = LocalDate.now()
                            .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                            .plusWeeks(week);

                    generateWeeklySlots(doctor, targetDate, range.getStart(), range.getEnd(), duration);
                }
            }
        });
    }

    @Override
    public void updateAvailability(Long id, DoctorAvailabilityDto dto) {
        defineAvailability(id, dto); // reuse template method
    }

    @Override
    public List<SlotResponseDto> getSlotsForDate(Long doctorId, LocalDate date) {
        validateDoctor(doctorId);
        List<Slot> slots = slotRepository.findByDoctorIdAndDate(doctorId, date);
        log.info("✅ Fetched {} slots for doctorId={} on {}", slots.size(), doctorId, date);
        return slots.stream()
                .map(slot -> modelMapper.map(slot, SlotResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateDoctor(Long id) {
        User user = validateDoctor(id);
        user.setEnabled(false);
        userRepository.save(user);
        log.warn("⚠️ Doctor with ID={} has been deactivated", id);
    }

    @Override
    public void deleteDoctor(Long id) {
        User user = validateDoctor(id);
        userRepository.delete(user);
        log.warn("🗑️ Doctor with ID={} has been deleted", id);
    }

    // 🔁 Template Method to generate and replace slots for a day
    private void generateWeeklySlots(User doctor, LocalDate date, LocalTime start, LocalTime end, int duration) {
        log.info("📅 Generating slots for Doctor={} Date={} Time={}–{}", doctor.getId(), date, start, end);

        // Delete existing slots
        List<Slot> existing = slotRepository.findByDoctorIdAndDate(doctor.getId(), date);
        if (!existing.isEmpty()) {
            slotRepository.deleteAll(existing);
            log.warn("🧹 Deleted {} old slots for Doctor={} Date={}", existing.size(), doctor.getId(), date);
        }

        // Create new slots
        List<Slot> slots = new ArrayList<>();
        LocalTime current = start;
        while (!current.plusMinutes(duration).isAfter(end)) {
            slots.add(Slot.builder()
                    .doctor(doctor)
                    .date(date)
                    .startTime(current)
                    .endTime(current.plusMinutes(duration))
                    .status(SlotStatus.AVAILABLE)
                    .type(SlotType.REGULAR)
                    .build());
            current = current.plusMinutes(duration);
        }

        slotRepository.saveAll(slots);
        log.info("✅ Created {} new slots for Doctor={} Date={}", slots.size(), doctor.getId(), date);
    }

    private User validateDoctor(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id=", "id", id));

        boolean isDoctor = user.getRoles().stream().anyMatch(r -> r.getName() == ERole.DOCTOR);
        if (!isDoctor) {
            log.error("❌ User ID={} is not a doctor", id);
            throw new HospitalAPIException(HttpStatus.BAD_REQUEST, "User is not a doctor");
        }
        return user;
    }
}
