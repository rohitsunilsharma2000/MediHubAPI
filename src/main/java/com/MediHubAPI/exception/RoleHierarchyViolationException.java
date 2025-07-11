package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class RoleHierarchyViolationException extends HospitalAPIException {
    public RoleHierarchyViolationException() {
        super(HttpStatus.FORBIDDEN, "Role hierarchy violation");
    }
}