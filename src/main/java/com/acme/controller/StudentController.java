package com.acme.controller;

import com.acme.dto.response.StudentResponse;
import com.acme.service.UniversityService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@ApiResponse(responseCode = "429", description = "Rate limit exceeded")
public class StudentController {

    private final UniversityService universityService;

    public StudentController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @GetMapping("/{studentId}")
    @RateLimiter(name = "acme-api")
    @ApiResponse(responseCode = "404", description = "Student not found")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String studentId) {
        StudentResponse student = universityService.getStudentById(studentId);
        return ResponseEntity.ok(student);
    }
} 