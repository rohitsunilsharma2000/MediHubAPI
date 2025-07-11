package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class PasswordPolicyException extends HospitalAPIException {
    public PasswordPolicyException(String requirement) {
        super(HttpStatus.BAD_REQUEST, "Password doesn't meet requirement: " + requirement);
    }
}
