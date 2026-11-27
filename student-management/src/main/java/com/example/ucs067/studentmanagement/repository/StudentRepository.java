package com.example.ucs067.studentmanagement.repository;

import java.util.List;

import com.example.ucs067.studentmanagement.Student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByName(String name);

    List<Student> findByNameContaining(String keyword);

    List<Student> findByEmail(String email);

}
