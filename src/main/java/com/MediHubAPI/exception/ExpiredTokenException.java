package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class ExpiredTokenException extends HospitalAPIException {
    public ExpiredTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Authentication token has expired");
    }
}
