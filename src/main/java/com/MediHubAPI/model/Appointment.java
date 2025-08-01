package com.MediHubAPI.model;

import com.MediHubAPI.model.enums.AppointmentStatus;
import com.MediHubAPI.model.enums.AppointmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "appointments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"doctor_id", "appointment_date", "slot_time"}),
                @UniqueConstraint(columnNames = {"patient_id", "appointment_date", "slot_time"})
        }
)
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    private User patient;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private Slot slot;

    // Denormalized for reporting/filtering
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime; // denotes the time of the slot (usually slot.startTime)

    //Track the original appointment when a new one is created via rescheduling.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_from")
    private Appointment rescheduledFrom;

}