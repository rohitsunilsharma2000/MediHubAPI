package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends HospitalAPIException {
    public BusinessRuleViolationException(String rule) {
        super(HttpStatus.CONFLICT, "Violates business rule: " + rule);
    }
}
