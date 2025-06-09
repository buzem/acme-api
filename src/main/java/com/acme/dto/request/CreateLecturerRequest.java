package com.acme.dto.request;

import com.acme.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateLecturerRequest(
        @NotBlank(message = "Name " + ValidationConstants.NOT_BLANK_MESSAGE)
        @Pattern(regexp = ValidationConstants.ALPHANUMERIC_PATTERN, message = "Name " + ValidationConstants.ALPHANUMERIC_MESSAGE)
        String name,
        
        @NotBlank(message = "Surname " + ValidationConstants.NOT_BLANK_MESSAGE)
        @Pattern(regexp = ValidationConstants.ALPHANUMERIC_PATTERN, message = "Surname " + ValidationConstants.ALPHANUMERIC_MESSAGE)
        String surname,
        
        @NotBlank(message = ValidationConstants.LECTURER_ID_MESSAGE)
        @Pattern(regexp = ValidationConstants.ALPHANUMERIC_PATTERN, message = "Lecturer ID " + ValidationConstants.ALPHANUMERIC_MESSAGE)
        String lecturerId
) {} 