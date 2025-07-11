package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class UsernameExistsException extends HospitalAPIException {
    public UsernameExistsException(String username) {
        super(HttpStatus.CONFLICT, "Username '" + username + "' already exists");
    }
}
