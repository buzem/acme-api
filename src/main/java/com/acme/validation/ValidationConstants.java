package com.acme.validation;

public final class ValidationConstants {
    
    private ValidationConstants() {
    }
    
    public static final String ALPHANUMERIC_PATTERN = "^[a-zA-Z0-9]+$";
    public static final String ALPHANUMERIC_MESSAGE = "must contain only alphanumeric characters";
    
    public static final String NOT_BLANK_MESSAGE = "cannot be blank";

    public static final String LECTURER_ID_MESSAGE = "Lecturer ID " + NOT_BLANK_MESSAGE;
    public static final String STUDENT_ID_MESSAGE = "Student ID " + NOT_BLANK_MESSAGE;
}