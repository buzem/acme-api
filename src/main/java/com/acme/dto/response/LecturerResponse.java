package com.acme.dto.response;

import com.acme.dto.summary.StudentSummaryDto;
import com.acme.entity.Lecturer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LecturerResponse(
    @JsonProperty("lecturerId")
    String lecturerId,

    @JsonProperty("name")
    String name,

    @JsonProperty("surname")
    String surname,

    @JsonProperty("students")
    List<StudentSummaryDto> students
) {
    
    public static LecturerResponse from(Lecturer lecturer) {
        List<StudentSummaryDto> studentSummaries = lecturer.getStudents().stream()
                .map(StudentSummaryDto::from)
                .toList();
                
        return new LecturerResponse(
                lecturer.getLecturerId(),
                lecturer.getName(),
                lecturer.getSurname(),
                studentSummaries
        );
    }
} 