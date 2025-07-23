package com.MediHubAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String firstName;
    private String lastName;

    private String mobileNumber;
    private String landlineNumber;
    private String fileNumber;

    private LocalDate dateOfBirth;
    private Integer ageYears;
    private Integer ageMonths;
    private Integer ageDays;

    @Enumerated(EnumType.STRING)
    private Gender sex;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    private String motherTongue;

    private String govtIdType;
    private String govtIdNumber;

    //private String hospitalId;
    private String otherHospitalIds;

    private String email;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private Referrer referrer;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientAddress address;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientPhoto photo;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientDetails details;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientNotes notes;
}

