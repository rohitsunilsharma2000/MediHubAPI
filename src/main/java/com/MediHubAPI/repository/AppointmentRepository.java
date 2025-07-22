package com.MediHubAPI.repository;


import com.MediHubAPI.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


import com.MediHubAPI.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    boolean existsByPatientAndAppointmentDateAndSlotTime(User patient, LocalDate date, LocalTime time);

    boolean existsByDoctorAndAppointmentDateAndSlotTime(User doctor, LocalDate date, LocalTime time);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    @Query("""
                SELECT a FROM Appointment a
                WHERE a.patient.id = :patientId
                AND (:cursor IS NULL OR a.appointmentDate < :cursorDate OR 
                     (a.appointmentDate = :cursorDate AND a.id < :cursorId))
                ORDER BY a.appointmentDate DESC, a.id DESC
            """)
    List<Appointment> findAppointmentsCursorBased(
            @Param("patientId") Long patientId,
            @Param("cursorDate") LocalDate cursorDate,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );


}
