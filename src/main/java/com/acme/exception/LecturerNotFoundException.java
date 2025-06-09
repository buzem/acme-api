package com.acme.exception;

public class LecturerNotFoundException extends RuntimeException {
    public LecturerNotFoundException(String message) {
        super(message);
    }
} 