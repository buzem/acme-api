package com.acme.integration;

import com.acme.dto.request.CreateLecturerRequest;
import com.acme.dto.request.CreateStudentRequest;
import com.acme.dto.response.LecturerResponse;
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
@DisplayName("Lecturer Integration Tests")
class LecturerIntegrationTest {

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

    @Nested
    @DisplayName("Create Lecturer")
    class CreateLecturerTests {

        @Test
        @DisplayName("Should create lecturer with valid data")
        void shouldCreateLecturer() throws Exception {
            CreateLecturerRequest request = new CreateLecturerRequest("Max", "Mustermann", "PROFMAXMUSTERMANN");

            MvcResult result = mockMvc.perform(post("/lecturers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Max"))
                    .andExpect(jsonPath("$.surname").value("Mustermann"))
                    .andExpect(jsonPath("$.lecturerId").value("PROFMAXMUSTERMANN"))
                    .andExpect(jsonPath("$.lecturerId").exists())
                    .andExpect(jsonPath("$.students").isArray())
                    .andExpect(jsonPath("$.students").isEmpty())
                    .andReturn();

            LecturerResponse lecturer = fromJson(result.getResponse().getContentAsString(), LecturerResponse.class);
            assertThat(lecturer.lecturerId()).isNotNull();
            assertThat(lecturer.students()).isEmpty();
        }

        @Test
        @DisplayName("Should return conflict when creating duplicate lecturer")
        void shouldReturnConflictWhenCreatingDuplicateLecturer() throws Exception {
            CreateLecturerRequest request = new CreateLecturerRequest("Max", "Mustermann", "PROFMAXMUSTERMANN");

            // Create first lecturer
            mockMvc.perform(post("/lecturers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/lecturers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Lecturer with ID 'PROFMAXMUSTERMANN' already exists"));
        }

        @Test
        @DisplayName("Should return bad request for invalid lecturer data")
        void shouldReturnBadRequestForInvalidLecturerData() throws Exception {
            CreateLecturerRequest request = new CreateLecturerRequest("", "InvalidName!", "INVALID123");

            mockMvc.perform(post("/lecturers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Input validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").exists())
                    .andExpect(jsonPath("$.fieldErrors.surname").value("Surname must contain only alphanumeric characters"));
        }
    }

    @Nested
    @DisplayName("Get Lecturer")
    class GetLecturerTests {

        @Test
        @DisplayName("Should retrieve lecturer by ID")
        void shouldRetrieveLecturerById() throws Exception {
            LecturerResponse createdLecturer = createLecturer("Jane", "Smith");

            mockMvc.perform(get("/lecturers/{lecturerId}", createdLecturer.lecturerId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lecturerId").value(createdLecturer.lecturerId()))
                    .andExpect(jsonPath("$.name").value("Jane"))
                    .andExpect(jsonPath("$.surname").value("Smith"))
                    .andExpect(jsonPath("$.students").isArray());
        }

        @Test
        @DisplayName("Should return not found for non-existent lecturer")
        void shouldReturnNotFoundForNonExistentLecturer() throws Exception {
            mockMvc.perform(get("/lecturers/{lecturerId}", NON_EXISTENT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Lecturer with ID 'NONEXISTENT999' not found"));
        }
    }

    @Nested
    @DisplayName("Add Student to Lecturer")
    class AddStudentToLecturerTests {

        @Test
        @DisplayName("Should add student to lecturer")
        void shouldAddStudentToLecturer() throws Exception {
            LecturerResponse lecturer = createLecturer("Prof", "Wilson");

            CreateStudentRequest studentRequest = new CreateStudentRequest("Alice", "Johnson", "STU001");
            mockMvc.perform(post("/lecturers/{lecturerId}/add", lecturer.lecturerId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(studentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Alice"))
                    .andExpect(jsonPath("$.surname").value("Johnson"))
                    .andExpect(jsonPath("$.studentId").exists())
                    .andExpect(jsonPath("$.lecturers[0].lecturerId").value(lecturer.lecturerId()));

            mockMvc.perform(get("/lecturers/{lecturerId}", lecturer.lecturerId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.students").isArray())
                    .andExpect(jsonPath("$.students[0].name").value("Alice"))
                    .andExpect(jsonPath("$.students[0].surname").value("Johnson"));
        }

        @Test
        @DisplayName("Should return not found when adding student to non-existent lecturer")
        void shouldReturnNotFoundWhenAddingStudentToNonExistentLecturer() throws Exception {
            CreateStudentRequest request = new CreateStudentRequest("Bob", "Brown", "STU002");

            mockMvc.perform(post("/lecturers/{lecturerId}/add", NON_EXISTENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Lecturer with ID 'NONEXISTENT999' not found"));
        }
    }
} 