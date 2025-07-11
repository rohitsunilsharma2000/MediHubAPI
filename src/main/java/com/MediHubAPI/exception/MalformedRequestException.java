package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class MalformedRequestException extends HospitalAPIException {
    public MalformedRequestException() {
        super(HttpStatus.BAD_REQUEST, "Request format is invalid");
    }
}