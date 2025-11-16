package com.student.management.service;

import com.student.management.dto.StudentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    StudentDTO createStudent(StudentDTO studentDTO);

    StudentDTO getStudentById(Long id);

    List<StudentDTO> getAllStudents();

    Page<StudentDTO> getAllStudentsPaginated(Pageable pageable);

    StudentDTO updateStudent(Long id, StudentDTO studentDTO);

    void deleteStudent(Long id);

    Page<StudentDTO> searchByName(String name, Pageable pageable);

    Page<StudentDTO> searchByCourse(String course, Pageable pageable);

    Page<StudentDTO> searchByKeyword(String keyword, Pageable pageable);
}
