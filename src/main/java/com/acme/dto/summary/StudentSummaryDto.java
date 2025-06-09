package com.acme.dto.summary;

import com.acme.entity.Student;
import com.fasterxml.jackson.annotation.JsonProperty;

public record StudentSummaryDto(
    @JsonProperty("studentId")
    String studentId,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("surname")
    String surname
) {
    
    public static StudentSummaryDto from(Student student) {
        return new StudentSummaryDto(
                student.getStudentId(),
                student.getName(),
                student.getSurname()
        );
    }
} 