package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoleException extends HospitalAPIException {
    public InvalidRoleException(String role) {
        super(HttpStatus.BAD_REQUEST, "Invalid role: " + role);
    }
}
