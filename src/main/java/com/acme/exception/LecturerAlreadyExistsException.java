package com.acme.exception;

public class LecturerAlreadyExistsException extends RuntimeException {
    public LecturerAlreadyExistsException(String message) {
        super(message);
    }
} 