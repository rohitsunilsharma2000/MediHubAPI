package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class MissingFieldException extends HospitalAPIException {
    public MissingFieldException(String field) {
        super(HttpStatus.BAD_REQUEST, "Missing required field: " + field);
    }
}
