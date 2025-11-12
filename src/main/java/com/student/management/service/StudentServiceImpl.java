package com.student.management.service;

import com.student.management.dto.StudentDTO;
import com.student.management.entity.Student;
import com.student.management.exception.DuplicateResourceException;
import com.student.management.exception.ResourceNotFoundException;
import com.student.management.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO) {
        // Check if email already exists
        if (studentRepository.findByEmail(studentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Student", "email", studentDTO.getEmail());
        }

        Student student = mapToEntity(studentDTO);
        Student savedStudent = studentRepository.save(student);
        return mapToDTO(savedStudent);
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToDTO(student);
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<StudentDTO> getAllStudentsPaginated(Pageable pageable) {
        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(this::mapToDTO);
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Check if email is being changed and if it already exists
        if (!student.getEmail().equals(studentDTO.getEmail())) {
            if (studentRepository.findByEmail(studentDTO.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Student", "email", studentDTO.getEmail());
            }
        }

        student.setName(studentDTO.getName());
        student.setEmail(studentDTO.getEmail());
        student.setCourse(studentDTO.getCourse());
        student.setAge(studentDTO.getAge());

        Student updatedStudent = studentRepository.save(student);
        return mapToDTO(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        studentRepository.delete(student);
    }

    @Override
    public Page<StudentDTO> searchByName(String name, Pageable pageable) {
        Page<Student> students = studentRepository.findByNameContainingIgnoreCase(name, pageable);
        return students.map(this::mapToDTO);
    }

    @Override
    public Page<StudentDTO> searchByCourse(String course, Pageable pageable) {
        Page<Student> students = studentRepository.findByCourseContainingIgnoreCase(course, pageable);
        return students.map(this::mapToDTO);
    }

    @Override
    public Page<StudentDTO> searchByKeyword(String keyword, Pageable pageable) {
        Page<Student> students = studentRepository.searchByNameOrCourse(keyword, pageable);
        return students.map(this::mapToDTO);
    }

    // Helper methods to convert between Entity and DTO
    private StudentDTO mapToDTO(Student student) {
        return new StudentDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCourse(),
                student.getAge()
        );
    }

    private Student mapToEntity(StudentDTO studentDTO) {
        return new Student(
                null,
                studentDTO.getName(),
                studentDTO.getEmail(),
                studentDTO.getCourse(),
                studentDTO.getAge()
        );
    }
}
