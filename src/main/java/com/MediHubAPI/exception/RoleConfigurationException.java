package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class RoleConfigurationException extends HospitalAPIException {
    public RoleConfigurationException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Role configuration error");
    }
}
