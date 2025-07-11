package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class InsufficientPrivilegesException extends HospitalAPIException {
    public InsufficientPrivilegesException(String requiredRole) {
        super(HttpStatus.FORBIDDEN, "Requires role: " + requiredRole);
    }
}
