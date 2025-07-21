package com.MediHubAPI.repository;


import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

//@Repository
//public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
//    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);
//}


import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    boolean existsByPatientAndAppointmentDateAndSlotTime(User patient, LocalDate date, LocalTime time);
    boolean existsByDoctorAndAppointmentDateAndSlotTime(User doctor, LocalDate date, LocalTime time);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);
}
