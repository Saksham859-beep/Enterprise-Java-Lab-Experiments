package com.example.ucs067.studentmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ValidationApiTests extends ApiTestSupport {

    @Test
    void blankNameAndBadEmailAreRejectedOnCreate() throws Exception {
        mockMvc.perform(post("/students")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":10,\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Student name is required"))
                .andExpect(jsonPath("$.email").value("Please provide a valid email address"));

        assertEquals(0, studentRepository.count());
    }

    @Test
    void missingNameIsRejectedOnCreate() throws Exception {
        mockMvc.perform(post("/students")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neha@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Student name is required"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void blankEmailIsRejectedOnCreate() throws Exception {
        mockMvc.perform(post("/students")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Neha Sharma\",\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Student email is required"));
    }

    @Test
    void validStudentIsAccepted() throws Exception {
        mockMvc.perform(post("/students")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Neha Sharma\",\"email\":\"neha@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neha Sharma"));

        assertEquals(1, studentRepository.count());
    }

    @Test
    void invalidUpdateIsRejectedAndRecordStaysUnchanged() throws Exception {
        Student saved = saveStudent("Neha Sharma", "neha@example.com");

        mockMvc.perform(put("/students/{id}", saved.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalid-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Student name is required"))
                .andExpect(jsonPath("$.email").value("Please provide a valid email address"));

        mockMvc.perform(get("/students/{id}", saved.getId()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neha Sharma"))
                .andExpect(jsonPath("$.email").value("neha@example.com"));
    }

}
