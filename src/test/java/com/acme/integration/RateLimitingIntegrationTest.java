package com.acme.integration;

import com.acme.dto.request.CreateLecturerRequest;
import com.acme.dto.response.LecturerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@DisplayName("Rate Limiting Integration Tests")
class RateLimitingIntegrationTest {
    
    private static final int RATE_LIMIT_REQUESTS = 20;
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitingIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createLecturerAndGetId() throws Exception {
        log.info("Creating a new lecturer for rate limit testing");
        CreateLecturerRequest request = new CreateLecturerRequest("Test", "Lecturer", "PROFTEST001");
        
        MvcResult result = mockMvc.perform(post("/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        LecturerResponse lecturer = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                LecturerResponse.class);
        log.info("Created lecturer with ID: {}", lecturer.lecturerId());
        return lecturer.lecturerId();
    }

    @Test
    @DisplayName("Should reset rate limit after 10 seconds")
    void shouldResetRateLimitAfter10Seconds() throws Exception {

        String lecturerId = createLecturerAndGetId(); // Request #1

        for (int i = 1; i < RATE_LIMIT_REQUESTS; i++) {
            mockMvc.perform(get("/lecturers/{lecturerId}", lecturerId))
                    .andExpect(status().isOk());
        }
        
        mockMvc.perform(get("/lecturers/{lecturerId}", lecturerId))
                .andExpect(status().isTooManyRequests());

        log.info("Waiting for 10 seconds for rate limit to reset...");
        TimeUnit.SECONDS.sleep(10);
        log.info("10 seconds elapsed - rate limit should be reset");
        
        log.info("Making request after rate limit reset - should succeed");
        mockMvc.perform(get("/lecturers/{lecturerId}", lecturerId))
                .andExpect(status().isOk());
        log.info("Request succeeded after rate limit reset - test passed");
    }

    
} 