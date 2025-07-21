package com.MediHubAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    private User patient;

    private LocalDate appointmentDate;

    private LocalTime slotTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    private AppointmentType type;

    private String rescheduledFrom;

    private String bookedBy;

    private String cancellationReason;
}
