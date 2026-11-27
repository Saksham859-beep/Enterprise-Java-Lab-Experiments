package com.example.ucs067.studentmanagement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentQueryApiTests extends ApiTestSupport {

    @BeforeEach
    void seedStudents() {
        saveStudent("Rahul", "rahul@example.com");
        saveStudent("Priya", "priya@example.com");
        saveStudent("Amit", "amit@example.com");
        saveStudent("Neha", "neha@example.com");
        saveStudent("Ravi", "ravi@example.com");
    }

    @Test
    void searchByNameReturnsExactMatch() throws Exception {
        mockMvc.perform(get("/students/search/name/{name}", "Rahul").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rahul"))
                .andExpect(jsonPath("$[0].email").value("rahul@example.com"));
    }

    @Test
    void searchByNameReturnsEmptyListWhenNothingMatches() throws Exception {
        mockMvc.perform(get("/students/search/name/{name}", "Unknown").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchByNameContainingReturnsPartialMatches() throws Exception {
        mockMvc.perform(get("/students/search/name/contains/{keyword}", "Ra").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchByEmailReturnsMatchingStudent() throws Exception {
        mockMvc.perform(get("/students/search/email/{email}", "neha@example.com").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Neha"));
    }

    @Test
    void firstPageContainsRequestedNumberOfStudents() throws Exception {
        mockMvc.perform(get("/students/page")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "name")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Amit"))
                .andExpect(jsonPath("$.content[1].name").value("Neha"));
    }

    @Test
    void lastPageContainsRemainingStudents() throws Exception {
        mockMvc.perform(get("/students/page")
                        .param("page", "2")
                        .param("size", "2")
                        .param("sort", "name")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ravi"));
    }

    @Test
    void studentsAreSortedByName() throws Exception {
        mockMvc.perform(get("/students/sort/name").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Amit"))
                .andExpect(jsonPath("$[1].name").value("Neha"))
                .andExpect(jsonPath("$[2].name").value("Priya"))
                .andExpect(jsonPath("$[3].name").value("Rahul"))
                .andExpect(jsonPath("$[4].name").value("Ravi"));
    }

}
