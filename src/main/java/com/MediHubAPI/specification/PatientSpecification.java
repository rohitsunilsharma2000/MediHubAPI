package com.MediHubAPI.specification;

import com.MediHubAPI.model.Patient;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    public static Specification<Patient> contains(String field, String value) {
        return (root, query, cb) ->
                value == null ? null : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }
}
