package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends HospitalAPIException {
    public UnauthorizedAccessException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
