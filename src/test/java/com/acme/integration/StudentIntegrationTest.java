package com.acme.integration;

import com.acme.dto.request.CreateLecturerRequest;
import com.acme.dto.request.CreateStudentRequest;
import com.acme.dto.response.LecturerResponse;
import com.acme.dto.response.StudentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@DisplayName("Student Integration Tests")
class StudentIntegrationTest {

    private static final String NON_EXISTENT_ID = "NONEXISTENT999";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    private LecturerResponse createLecturer(String name, String surname) throws Exception {
        String lecturerId = "PROF" + name.toUpperCase() + surname.toUpperCase();
        CreateLecturerRequest request = new CreateLecturerRequest(name, surname, lecturerId);
        MvcResult result = mockMvc.perform(post("/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return fromJson(result.getResponse().getContentAsString(), LecturerResponse.class);
    }

    private StudentResponse addStudentToLecturer(String lecturerId, String name, String surname) throws Exception {
        String studentId = "STU" + name.toUpperCase() + surname.toUpperCase();
        CreateStudentRequest request = new CreateStudentRequest(name, surname, studentId);
        MvcResult result = mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return fromJson(result.getResponse().getContentAsString(), StudentResponse.class);
    }

    @Nested
    @DisplayName("Get Student")
    class GetStudentTests {

        @Test
        @DisplayName("Should retrieve student by ID")
        void shouldRetrieveStudentById() throws Exception {
            LecturerResponse lecturer = createLecturer("Prof", "Johnson");

            StudentResponse student = addStudentToLecturer(lecturer.lecturerId(), "Max", "Mustermann");

            mockMvc.perform(get("/students/{studentId}", student.studentId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studentId").value(student.studentId()))
                    .andExpect(jsonPath("$.name").value("Max"))
                    .andExpect(jsonPath("$.surname").value("Mustermann"))
                    .andExpect(jsonPath("$.lecturers").isArray())
                    .andExpect(jsonPath("$.lecturers[0].lecturerId").value(lecturer.lecturerId()));
        }

        @Test
        @DisplayName("Should return not found for non-existent student")
        void shouldReturnNotFoundForNonExistentStudent() throws Exception {
            mockMvc.perform(get("/students/{studentId}", NON_EXISTENT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Student with ID 'NONEXISTENT999' not found"));
        }
    }

    @Nested
    @DisplayName("Student-Lecturer Relationships")
    class StudentLecturerRelationshipTests {

        @Test
        @DisplayName("Should reuse existing student when assigning to new lecturer")
        void shouldReuseExistingStudentWhenAssigningToNewLecturer() throws Exception {
            LecturerResponse lecturer1 = createLecturer("Prof", "Alpha");
            LecturerResponse lecturer2 = createLecturer("Prof", "Beta");

            StudentResponse student1 = addStudentToLecturer(lecturer1.lecturerId(), "Erika", "Musterfrau");

            // Add SAME student (same studentId) to second lecturer - should reuse existing student
            StudentResponse student2 = addStudentToLecturer(lecturer2.lecturerId(), "Erika", "Musterfrau");

            assertThat(student1.studentId()).isEqualTo(student2.studentId());

            mockMvc.perform(get("/students/{studentId}", student1.studentId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lecturers").isArray())
                    .andExpect(jsonPath("$.lecturers").isNotEmpty());

            mockMvc.perform(get("/lecturers/{lecturerId}", lecturer1.lecturerId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.students[0].studentId").value(student1.studentId()));

            mockMvc.perform(get("/lecturers/{lecturerId}", lecturer2.lecturerId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.students[0].studentId").value(student1.studentId()));
        }

        @Test
        @DisplayName("Should return conflict when assigning same student to same lecturer twice")
        void shouldReturnConflictWhenAssigningSameStudentToSameLecturerTwice() throws Exception {
            LecturerResponse lecturer = createLecturer("Prof", "Gamma");

            CreateStudentRequest studentRequest = new CreateStudentRequest("Hans", "Mueller", "STUHANSMUEL");
            mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturer.lecturerId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(studentRequest)))
                    .andExpect(status().isCreated());

            // Try to add same student again - should return conflict
            mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturer.lecturerId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(studentRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Student with ID 'STUHANSMUEL' is already assigned to this lecturer"));
        }

        @Test
        @DisplayName("Should return conflict when studentId exists with different name/surname")
        void shouldReturnConflictWhenStudentIdExistsWithDifferentNameSurname() throws Exception {
            LecturerResponse lecturer1 = createLecturer("Prof", "Alpha");
            LecturerResponse lecturer2 = createLecturer("Prof", "Beta");

            CreateStudentRequest studentRequest1 = new CreateStudentRequest("Mr", "John", "STU001");
            mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturer1.lecturerId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(studentRequest1)))
                    .andExpect(status().isCreated());

            // Try to add different student with same studentId - should return conflict
            CreateStudentRequest studentRequest2 = new CreateStudentRequest("Jane", "Smith", "STU001");
            mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturer2.lecturerId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(studentRequest2)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Student with ID 'STU001' already exists with different name/surname"));
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should return bad request for invalid student data")
        void shouldReturnBadRequestForInvalidStudentData() throws Exception {
            LecturerResponse lecturer = createLecturer("Prof", "Delta");

            CreateStudentRequest invalidRequest = new CreateStudentRequest("", "InvalidName!", "STU999");

            mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturer.lecturerId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Input validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").exists())
                    .andExpect(jsonPath("$.fieldErrors.surname").value("Surname must contain only alphanumeric characters"));
        }
    }
}