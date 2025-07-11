package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends HospitalAPIException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}