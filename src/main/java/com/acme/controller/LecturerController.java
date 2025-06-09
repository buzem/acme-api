package com.acme.controller;

import com.acme.dto.request.CreateLecturerRequest;
import com.acme.dto.request.CreateStudentRequest;
import com.acme.dto.response.LecturerResponse;
import com.acme.dto.response.StudentResponse;
import com.acme.service.UniversityService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/lecturers")
@ApiResponses(value = {
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
})
public class LecturerController {

    private final UniversityService universityService;

    public LecturerController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @PostMapping
    @RateLimiter(name = "acme-api")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Lecturer created successfully"),
        @ApiResponse(responseCode = "409", description = "Lecturer already exists")
    })
    public ResponseEntity<LecturerResponse> createLecturer(@Valid @RequestBody CreateLecturerRequest request) {
        LecturerResponse lecturer = universityService.createLecturer(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{lecturerId}")
                .buildAndExpand(request.lecturerId())
                .toUri();
        
        return ResponseEntity.created(location).body(lecturer);
    }

    @GetMapping("/{lecturerId}")
    @RateLimiter(name = "acme-api")
    @ApiResponse(responseCode = "404", description = "Lecturer not found")
    public ResponseEntity<LecturerResponse> getLecturer(@PathVariable String lecturerId) {
        LecturerResponse lecturer = universityService.getLecturerById(lecturerId);
        return ResponseEntity.ok(lecturer);
    }

    @PostMapping("/{lecturerId}/add")
    @RateLimiter(name = "acme-api")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Student added to lecturer"),
        @ApiResponse(responseCode = "404", description = "Lecturer not found"),
        @ApiResponse(responseCode = "409", description = "Student already assigned to lecturer or student ID exists with different name/surname")
    })
    public ResponseEntity<StudentResponse> addStudentToLecturer(
            @PathVariable String lecturerId,
            @Valid @RequestBody CreateStudentRequest request) {
        
        StudentResponse student = universityService.addStudentToLecturer(lecturerId, request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/students/{studentId}")
                .buildAndExpand(student.studentId())
                .toUri();
        
        return ResponseEntity.created(location).body(student);
    }
} 