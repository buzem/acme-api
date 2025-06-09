package com.acme.repository;

import com.acme.entity.Lecturer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    
    boolean existsByLecturerId(String lecturerId);
    
    Optional<Lecturer> findByLecturerId(String lecturerId);

    @Query("SELECT l FROM Lecturer l WHERE l.lecturerId = :lecturerId")
    @EntityGraph(attributePaths = {"students"})
    Optional<Lecturer> findLecturerWithStudents(@Param("lecturerId") String lecturerId);
}