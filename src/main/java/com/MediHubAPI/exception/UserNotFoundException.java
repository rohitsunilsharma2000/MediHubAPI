package com.MediHubAPI.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long userId) {
        super("User", "id", userId);
    }
}
