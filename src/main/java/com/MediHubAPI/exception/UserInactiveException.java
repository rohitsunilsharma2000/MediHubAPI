package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class UserInactiveException extends HospitalAPIException {
    public UserInactiveException() {
        super(HttpStatus.FORBIDDEN, "User account is inactive");
    }
}
