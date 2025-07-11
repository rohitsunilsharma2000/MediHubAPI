package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class RoleConflictException extends HospitalAPIException {
    public RoleConflictException(String conflict) {
        super(HttpStatus.CONFLICT, "Role conflict: " + conflict);
    }
}