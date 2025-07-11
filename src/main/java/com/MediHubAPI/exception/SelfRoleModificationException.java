package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class SelfRoleModificationException extends HospitalAPIException {
    public SelfRoleModificationException() {
        super(HttpStatus.FORBIDDEN, "Cannot modify your own role");
    }
}