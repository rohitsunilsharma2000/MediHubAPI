package com.MediHubAPI.service.impl;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.exception.ResourceNotFoundException;
import com.MediHubAPI.model.ERole;
import com.MediHubAPI.model.Slot;
import com.MediHubAPI.model.User;
import com.MediHubAPI.model.enums.SlotStatus;
import com.MediHubAPI.model.enums.SlotType;
import com.MediHubAPI.repository.SlotRepository;
import com.MediHubAPI.repository.UserRepository;
import com.MediHubAPI.service.DoctorService;
import com.MediHubAPI.specification.DoctorSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        for (Map.Entry<DayOfWeek, List<DoctorAvailabilityDto.TimeRange>> entry : availabilityMap.entrySet()) {
            DayOfWeek day = entry.getKey();
            List<DoctorAvailabilityDto.TimeRange> ranges = entry.getValue();

            for (DoctorAvailabilityDto.TimeRange range : ranges) {
                LocalTime start = range.getStart();
                LocalTime end = range.getEnd();

                // Generate slots for next 4 weeks for this day
                for (int week = 0; week < 4; week++) {
                    LocalDate targetDate = LocalDate.now()
                            .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                            .plusWeeks(week);

                    log.info("Generating slots for Doctor={}, Day={}, Date={}, Start={}, End={}",
                            doctorId, day, targetDate, start, end);

                    List<Slot> slots = new ArrayList<>();

                    LocalTime current = start;
                    while (current.plusMinutes(duration).compareTo(end) <= 0) {
                        Slot slot = Slot.builder()
                                .doctor(doctor)
                                .date(targetDate)
                                .startTime(current)
                                .endTime(current.plusMinutes(duration))
                                .status(SlotStatus.AVAILABLE)
                                .type(SlotType.REGULAR)
                                .build();

                        slots.add(slot);
                        current = current.plusMinutes(duration);
                    }

                    // Delete old slots first
                    List<Slot> existing = slotRepository.findByDoctorIdAndDate(doctorId, targetDate);
                    if (!existing.isEmpty()) {
                        slotRepository.deleteAll(existing);
                        log.warn("Deleted {} slots for Doctor={}, Date={}", existing.size(), doctorId, targetDate);
                    }

                    slotRepository.saveAll(slots);
                    log.info("Created {} new slots for Doctor={}, Date={}", slots.size(), doctorId, targetDate);
                }
            }
        }
    }

    @Override
    public void updateAvailability(Long id, DoctorAvailabilityDto dto) {
//        log.info("Updating availability for doctorId={} on {}", id, dto.getDate());
        defineAvailability(id, dto);
    }

    @Override
    public List<SlotResponseDto> getSlotsForDate(Long doctorId, LocalDate date) {
        validateDoctor(doctorId);
        List<Slot> slots = slotRepository.findByDoctorIdAndDate(doctorId, date);
        log.info("Fetched {} slots for doctorId={} on {}", slots.size(), doctorId, date);

        return slots.stream()
                .map(slot -> modelMapper.map(slot, SlotResponseDto.class))
                .toList();
    }


    @Override
    public void deactivateDoctor(Long id) {
        User user = validateDoctor(id);
        user.setEnabled(false);
        userRepository.save(user);
        log.warn("Doctor with ID={} has been deactivated", id);
    }

    @Override
    public void deleteDoctor(Long id) {
        User user = validateDoctor(id);
        userRepository.delete(user);
        log.warn("Doctor with ID={} has been deleted", id);
    }

    private User validateDoctor(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id=","id" , id));
        boolean isDoctor = user.getRoles().stream().anyMatch(r -> r.getName() == ERole.DOCTOR);
        if (!isDoctor) {
            log.error("User id={} is not a doctor", id);
            throw new HospitalAPIException(HttpStatus.BAD_REQUEST, "User is not a doctor");
        }
        return user;
    }
}