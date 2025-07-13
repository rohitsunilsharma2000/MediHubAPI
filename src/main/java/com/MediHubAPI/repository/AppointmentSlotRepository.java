package com.MediHubAPI.repository;

import com.MediHubAPI.model.AppointmentSlot;
import com.MediHubAPI.model.DoctorAvailability;
import com.MediHubAPI.model.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByDoctorAvailability(DoctorAvailability availability);

    List<AppointmentSlot> findByDoctorAvailabilityDateAndDoctorAvailabilityDoctorIdAndStatus(
            LocalDate date, Long doctorId, SlotStatus status);

    Optional<AppointmentSlot> findByIdAndStatus(Long id, SlotStatus status);

    boolean existsByDoctorAvailabilityAndStartTimeAndEndTime(DoctorAvailability availability,
                                                             java.time.LocalTime startTime,
                                                             java.time.LocalTime endTime);
}
