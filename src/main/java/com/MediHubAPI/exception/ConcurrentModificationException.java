package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class ConcurrentModificationException extends HospitalAPIException {
    public ConcurrentModificationException() {
        super(HttpStatus.CONFLICT, "Resource modified by another request");
    }
}

