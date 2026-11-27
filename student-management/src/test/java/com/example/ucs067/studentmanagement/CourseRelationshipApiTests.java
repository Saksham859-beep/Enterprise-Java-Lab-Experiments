package com.example.ucs067.studentmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CourseRelationshipApiTests extends ApiTestSupport {

    @Test
    void courseIsAssignedToStudent() throws Exception {
        Student rahul = saveStudent("Rahul", "rahul@example.com");

        mockMvc.perform(post("/students/{studentId}/courses", rahul.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Enterprise Java\",\"credits\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.courseName").value("Enterprise Java"))
                .andExpect(jsonPath("$.credits").value(4))
                .andExpect(jsonPath("$.student").doesNotExist());

        assertEquals(1, courseRepository.count());
    }

    @Test
    void oneStudentCanHaveManyCourses() throws Exception {
        Student rahul = saveStudent("Rahul", "rahul@example.com");

        mockMvc.perform(post("/students/{studentId}/courses", rahul.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Enterprise Java\",\"credits\":4}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/students/{studentId}/courses", rahul.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Database Management Systems\",\"credits\":3}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/students/{id}", rahul.getId()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rahul"))
                .andExpect(jsonPath("$.courses.length()").value(2))
                .andExpect(jsonPath("$.courses[?(@.courseName == 'Enterprise Java')].credits").value(4))
                .andExpect(jsonPath("$.courses[?(@.courseName == 'Database Management Systems')].credits").value(3));
    }

    @Test
    void addingCourseToUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(post("/students/{studentId}/courses", 9999)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Enterprise Java\",\"credits\":4}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void courseListIncludesEveryCourse() throws Exception {
        Student rahul = saveStudent("Rahul", "rahul@example.com");
        Student priya = saveStudent("Priya", "priya@example.com");

        mockMvc.perform(post("/students/{studentId}/courses", rahul.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Enterprise Java\",\"credits\":4}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/students/{studentId}/courses", priya.getId())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Python\",\"credits\":3}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/courses").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deletingStudentAlsoDeletesTheirCourses() throws Exception {
        Student rahul = new Student("Rahul", "rahul@example.com");
        Course java = new Course("Enterprise Java", 4);
        java.setStudent(rahul);
        rahul.getCourses().add(java);
        Student saved = studentRepository.save(rahul);

        assertEquals(1, courseRepository.count());

        mockMvc.perform(delete("/students/{id}", saved.getId()).with(asAdmin()))
                .andExpect(status().isOk());

        assertEquals(0, courseRepository.count());
    }

}
