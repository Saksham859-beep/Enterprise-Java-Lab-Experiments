package com.example.ucs067.studentmanagement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SecurityApiTests extends ApiTestSupport {

    @Test
    void helloEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring Boot!"));
    }

    @Test
    void studentsEndpointRejectsRequestsWithoutCredentials() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentsEndpointAcceptsCorrectCredentials() throws Exception {
        mockMvc.perform(get("/students").with(asAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    void studentsEndpointRejectsWrongPassword() throws Exception {
        mockMvc.perform(get("/students").with(withCredentials(ADMIN_USER, "wrong123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentsEndpointRejectsUnknownUser() throws Exception {
        mockMvc.perform(get("/students").with(withCredentials("guest", ADMIN_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void coursesEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/courses").with(asAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    void writeRequestsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Neha Sharma\",\"email\":\"neha@example.com\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/students/1"))
                .andExpect(status().isUnauthorized());
    }

}
