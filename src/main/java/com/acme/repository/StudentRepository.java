package com.acme.repository;

import com.acme.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);
    
    @Query("SELECT s FROM Student s WHERE s.studentId = :studentId")
    @EntityGraph(attributePaths = {"lecturers"})
    Optional<Student> findStudentWithLecturers(@Param("studentId") String studentId);
} 