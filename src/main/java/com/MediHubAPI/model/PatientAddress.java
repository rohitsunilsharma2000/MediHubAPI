package com.MediHubAPI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "patient_addresses")
@Data
public class PatientAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String area;
    private String city;
    private String pinCode;

    private String state;
    private String country;
    private String nationality;

    private boolean internationalPatient;

    @OneToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
}
