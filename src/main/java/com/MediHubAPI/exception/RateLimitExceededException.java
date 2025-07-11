package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends HospitalAPIException {
    public RateLimitExceededException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
    }
}