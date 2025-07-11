package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class RolePermissionException extends HospitalAPIException {
    public RolePermissionException(HttpStatus status, String message) {
        super(status, message);
    }
}
