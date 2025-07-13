package com.MediHubAPI.repository;

import com.MediHubAPI.model.DoctorAvailability;
import com.MediHubAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorAndDate(User doctor, LocalDate date);

    boolean existsByDoctorAndDate(User doctor, LocalDate date);
}
