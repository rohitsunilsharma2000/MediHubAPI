package com.MediHubAPI.util;

import com.MediHubAPI.model.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

public class PatientToUserMapper {

    public static User convertPatientToUser(Patient patient, Role patientRole) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient is null");
        }

        User user = new User();

        user.setUsername(generateUsername(patient));
        user.setEmail(patient.getEmail());
        user.setFirstName(patient.getFirstName());
        user.setLastName(patient.getLastName());
        user.setEnabled(true);
        user.setActivationDate(LocalDate.now());

        // Default password – should be updated via email link or set manually
        user.setPassword("{noop}default123"); // Use password encoder later

        // Assign patient role
        user.setRoles(new HashSet<>(Collections.singletonList(patientRole)));

        return user;
    }

    private static String generateUsername(Patient patient) {
        String base = (patient.getFirstName() + patient.getLastName()).toLowerCase().replaceAll("\\s+", "");
        String mobile = patient.getMobileNumber() != null ? patient.getMobileNumber().substring(Math.max(0, patient.getMobileNumber().length() - 4)) : "0000";
        return base + "_" + mobile;
    }
}
