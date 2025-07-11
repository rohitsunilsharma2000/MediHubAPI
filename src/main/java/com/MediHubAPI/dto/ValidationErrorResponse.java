package com.MediHubAPI.dto;

import com.MediHubAPI.dto.ErrorResponse;

import java.time.Instant;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse(int status, String error, String message,
                                   String path, Instant timestamp,
                                   Map<String, String> fieldErrors) {
        super(status, error, message, path, timestamp);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
// Getter and setter for fieldErrors...
}