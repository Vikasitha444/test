package com.student.management.controller;

import com.student.management.dto.StudentDTO;
import com.student.management.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "APIs for managing students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Create a new student", description = "Creates a new student with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Student with email already exists")
    })
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO createdStudent = studentService.createStudent(studentDTO);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID", description = "Retrieves a student by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student found"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<StudentDTO> getStudentById(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @GetMapping
    @Operation(summary = "Get all students", description = "Retrieves all students with optional pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully")
    })
    public ResponseEntity<?> getAllStudents(
            @Parameter(description = "Enable pagination (true/false)")
            @RequestParam(required = false, defaultValue = "false") boolean paginated,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "Sort by field (e.g., name, email, course, age)")
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(required = false, defaultValue = "asc") String direction) {

        if (paginated) {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<StudentDTO> students = studentService.getAllStudentsPaginated(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("students", students.getContent());
            response.put("currentPage", students.getNumber());
            response.put("totalItems", students.getTotalElements());
            response.put("totalPages", students.getTotalPages());

            return ResponseEntity.ok(response);
        } else {
            List<StudentDTO> students = studentService.getAllStudents();
            return ResponseEntity.ok(students);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Updates an existing student's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<StudentDTO> updateStudent(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", description = "Deletes a student by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<Map<String, String>> deleteStudent(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long id) {
        studentService.deleteStudent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Student deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search students", description = "Search students by name, course, or keyword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Map<String, Object>> searchStudents(
            @Parameter(description = "Search by name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Search by course")
            @RequestParam(required = false) String course,
            @Parameter(description = "Search by keyword (searches both name and course)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(required = false, defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<StudentDTO> students;

        if (keyword != null && !keyword.trim().isEmpty()) {
            students = studentService.searchByKeyword(keyword, pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            students = studentService.searchByName(name, pageable);
        } else if (course != null && !course.trim().isEmpty()) {
            students = studentService.searchByCourse(course, pageable);
        } else {
            students = studentService.getAllStudentsPaginated(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("students", students.getContent());
        response.put("currentPage", students.getNumber());
        response.put("totalItems", students.getTotalElements());
        response.put("totalPages", students.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
