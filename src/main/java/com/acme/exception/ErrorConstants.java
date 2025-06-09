package com.acme.exception;


public final class ErrorConstants {

    private ErrorConstants() {}
    
    public static final String LECTURER_NOT_FOUND_MESSAGE = "Lecturer with ID '%s' not found";
    public static final String LECTURER_ALREADY_EXISTS_MESSAGE = 
            "Lecturer with ID '%s' already exists";
    
    public static final String STUDENT_NOT_FOUND_MESSAGE = "Student with ID '%s' not found";
    public static final String STUDENT_ALREADY_ASSIGNED_MESSAGE = 
            "Student with ID '%s' is already assigned to this lecturer";
    public static final String STUDENT_ID_CONFLICT_MESSAGE = 
            "Student with ID '%s' already exists with different name/surname";
    
    public static final String VALIDATION_FAILED_MESSAGE = "Input validation failed";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected error occurred";
    public static final String RATE_LIMIT_EXCEEDED_MESSAGE = 
            "Too many requests. You have exceeded the rate limit of 20 requests per 10 seconds. Please wait up to 10 seconds before trying again.";

    
    public static String formatLecturerNotFound(Object lecturerId) {
        return String.format(LECTURER_NOT_FOUND_MESSAGE, lecturerId);
    }
    
    public static String formatLecturerAlreadyExists(String lecturerId) {
        return String.format(LECTURER_ALREADY_EXISTS_MESSAGE, lecturerId);
    }
    
    public static String formatStudentNotFound(Object studentId) {
        return String.format(STUDENT_NOT_FOUND_MESSAGE, studentId);
    }
    
    public static String formatStudentAlreadyAssigned(String studentId) {
        return String.format(STUDENT_ALREADY_ASSIGNED_MESSAGE, studentId);
    }
    
    public static String formatStudentIdConflict(String studentId) {
        return String.format(STUDENT_ID_CONFLICT_MESSAGE, studentId);
    }
    
    public static String createRateLimitJsonWithoutRetry(String message, String timestamp) {
        return String.format("{\"message\":\"%s\",\"timestamp\":\"%s\"}", 
            message.replace("\"", "\\\""), timestamp);
    }
} 