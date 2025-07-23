package com.MediHubAPI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "patient_photos")
@Data
public class PatientPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath; // or store as byte[] blob with @Lob

    @OneToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
}


