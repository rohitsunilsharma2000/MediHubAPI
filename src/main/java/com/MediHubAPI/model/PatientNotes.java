package com.MediHubAPI.model;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "patient_notes")
@Data
public class PatientNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean needsAttention;

    @Column(length = 1000)
    private String notes;

    @OneToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
}


