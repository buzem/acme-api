package com.acme.dto.response;

import com.acme.dto.summary.LecturerSummaryDto;
import com.acme.entity.Student;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StudentResponse(
    @JsonProperty("studentId")
    String studentId,

    @JsonProperty("name")
    String name,

    @JsonProperty("surname")
    String surname,

    @JsonProperty("lecturers")
    List<LecturerSummaryDto> lecturers
) {
    
    public static StudentResponse from(Student student) {
        List<LecturerSummaryDto> lecturerSummaries = student.getLecturers().stream()
                .map(LecturerSummaryDto::from)
                .toList();
                
        return new StudentResponse(
                student.getStudentId(),
                student.getName(),
                student.getSurname(),
                lecturerSummaries
        );
    }
} 