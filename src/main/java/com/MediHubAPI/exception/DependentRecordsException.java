package com.MediHubAPI.exception;

import org.springframework.http.HttpStatus;

public class DependentRecordsException extends HospitalAPIException {
    public DependentRecordsException() {
        super(HttpStatus.CONFLICT, "Cannot delete - dependent records exist");
    }
}
