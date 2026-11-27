package com.example.ucs067.studentmanagement;

import java.nio.charset.StandardCharsets;

import com.example.ucs067.studentmanagement.repository.CourseRepository;
import com.example.ucs067.studentmanagement.repository.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestSupport {

    protected static final String ADMIN_USER = "admin";

    protected static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected StudentRepository studentRepository;

    @Autowired
    protected CourseRepository courseRepository;

    @BeforeEach
    void cleanDatabase() {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
    }

    protected static RequestPostProcessor asAdmin() {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, basicHeader(ADMIN_USER, ADMIN_PASSWORD));
            return request;
        };
    }

    protected static RequestPostProcessor withCredentials(String user, String password) {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, basicHeader(user, password));
            return request;
        };
    }

    protected Student saveStudent(String name, String email) {
        return studentRepository.save(new Student(name, email));
    }

    private static String basicHeader(String user, String password) {
        return "Basic " + HttpHeaders.encodeBasicAuth(user, password, StandardCharsets.UTF_8);
    }

}
