package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class HospitalAPIException extends RuntimeException {
    private final HttpStatus status;

    public HospitalAPIException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}