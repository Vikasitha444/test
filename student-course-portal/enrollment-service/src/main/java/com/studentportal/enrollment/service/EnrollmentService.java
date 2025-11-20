package com.studentportal.enrollment.service;

import com.studentportal.enrollment.client.CourseServiceClient;
import com.studentportal.enrollment.client.NotificationServiceClient;
import com.studentportal.enrollment.client.StudentServiceClient;
import com.studentportal.enrollment.dto.EnrollmentDTO;
import com.studentportal.enrollment.dto.EnrollmentRequestDTO;
import com.studentportal.enrollment.entity.Enrollment;
import com.studentportal.enrollment.exception.InvalidEnrollmentException;
import com.studentportal.enrollment.exception.ResourceNotFoundException;
import com.studentportal.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentServiceClient studentServiceClient;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    public EnrollmentDTO enrollStudent(EnrollmentRequestDTO request) {
        // Validate student exists
        if (!studentServiceClient.validateStudent(request.getStudentId())) {
            throw new InvalidEnrollmentException("Student not found with id: " + request.getStudentId());
        }

        // Validate course exists
        if (!courseServiceClient.validateCourse(request.getCourseId())) {
            throw new InvalidEnrollmentException("Course not found with id: " + request.getCourseId());
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(request.getStudentId());
        enrollment.setCourseId(request.getCourseId());
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus("ACTIVE");

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Send notification
        notificationServiceClient.sendEnrollmentNotification(
            request.getStudentId(),
            request.getCourseId()
        );

        return convertToDTO(savedEnrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudentId());
        dto.setCourseId(enrollment.getCourseId());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setStatus(enrollment.getStatus());
        return dto;
    }
}
