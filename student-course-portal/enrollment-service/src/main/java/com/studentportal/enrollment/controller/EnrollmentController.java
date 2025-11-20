package com.studentportal.enrollment.controller;

import com.studentportal.enrollment.dto.EnrollmentDTO;
import com.studentportal.enrollment.dto.EnrollmentRequestDTO;
import com.studentportal.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentDTO> enrollStudent(@Valid @RequestBody EnrollmentRequestDTO request) {
        EnrollmentDTO enrollment = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @GetMapping("/enrollments/student/{id}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByStudentId(@PathVariable Long id) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudentId(id);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/enrollments")
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        List<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(enrollments);
    }
}
