package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class EmailExistsException extends HospitalAPIException {
    public EmailExistsException(String email) {
        super(HttpStatus.CONFLICT, "Email '" + email + "' already registered");
    }
}