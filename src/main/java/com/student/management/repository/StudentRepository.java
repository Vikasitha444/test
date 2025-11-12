package com.student.management.repository;

import com.student.management.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    // Search by name (case-insensitive)
    Page<Student> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Search by course (case-insensitive)
    Page<Student> findByCourseContainingIgnoreCase(String course, Pageable pageable);

    // Search by name or course (case-insensitive)
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.course) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchByNameOrCourse(@Param("keyword") String keyword, Pageable pageable);
}
