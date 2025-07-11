package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends HospitalAPIException {
    public InvalidInputException(String field, String problem) {
        super(HttpStatus.BAD_REQUEST, "Invalid " + field + ": " + problem);
    }
}
