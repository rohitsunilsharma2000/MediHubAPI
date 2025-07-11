package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class FeatureDisabledException extends HospitalAPIException {
    public FeatureDisabledException(String feature) {
        super(HttpStatus.SERVICE_UNAVAILABLE, feature + " is currently disabled");
    }
}
