package com.example.ucs067.studentmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class StudentCrudApiTests extends ApiTestSupport {

    @Test
    void createStudentGeneratesId() throws Exception {
        mockMvc.perform(post("/students")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Karan Verma\",\"email\":\"karan@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Karan Verma"))
                .andExpect(jsonPath("$.email").value("karan@example.com"));

        assertEquals(1, studentRepository.count());
    }

    @Test
    void getAllStudentsReturnsEveryRecord() throws Exception {
        saveStudent("Rahul", "rahul@example.com");
        saveStudent("Priya", "priya@example.com");

        mockMvc.perform(get("/students").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getStudentByIdReturnsMatchingStudent() throws Exception {
        Student saved = saveStudent("Rahul", "rahul@example.com");

        mockMvc.perform(get("/students/{id}", saved.getId()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Rahul"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.courses.length()").value(0));
    }

    @Test
    void getUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/students/{id}", 9999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStudentChangesNameAndEmail() throws Exception {
        Student saved = saveStudent("Karan Verma", "karan@example.com");

        mockMvc.perform(put("/students/{id}", saved.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Karan Sharma\",\"email\":\"karan.sharma@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Karan Sharma"))
                .andExpect(jsonPath("$.email").value("karan.sharma@example.com"));

        Student reloaded = studentRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Karan Sharma", reloaded.getName());
        assertEquals("karan.sharma@example.com", reloaded.getEmail());
    }

    @Test
    void updateUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(put("/students/{id}", 9999)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nobody\",\"email\":\"nobody@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudentRemovesRecord() throws Exception {
        Student saved = saveStudent("Amit", "amit@example.com");

        mockMvc.perform(delete("/students/{id}", saved.getId()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string("Student deleted successfully"));

        assertTrue(studentRepository.findById(saved.getId()).isEmpty());

        mockMvc.perform(get("/students/{id}", saved.getId()).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/students/{id}", 9999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void sampleStudentEndpointReturnsRahul() throws Exception {
        mockMvc.perform(get("/student").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rahul"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"));
    }

}
