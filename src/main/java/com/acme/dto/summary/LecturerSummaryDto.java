package com.acme.dto.summary;

import com.acme.entity.Lecturer;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LecturerSummaryDto(
    @JsonProperty("lecturerId")
    String lecturerId,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("surname")
    String surname
) {
    
    public static LecturerSummaryDto from(Lecturer lecturer) {
        return new LecturerSummaryDto(
                lecturer.getLecturerId(),
                lecturer.getName(),
                lecturer.getSurname()
        );
    }
} 