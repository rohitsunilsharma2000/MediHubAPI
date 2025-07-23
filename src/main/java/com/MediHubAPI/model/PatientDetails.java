    package com.MediHubAPI.model;

    import jakarta.persistence.*;
    import lombok.Data;


    @Entity
    @Table(name = "patient_details")
    @Data
    public class PatientDetails {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String bloodGroup;

        private String fatherName;
        private String motherName;
        private String spouseName;

        private String alternateContact;
        private String education;
        private String occupation;
        private String religion;
        private String ivrLanguage;
        private Double birthWeight;

        @OneToOne
        @JoinColumn(name = "patient_id")
        private Patient patient;
    }

