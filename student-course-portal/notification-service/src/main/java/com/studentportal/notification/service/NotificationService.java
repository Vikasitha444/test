package com.studentportal.notification.service;

import com.studentportal.notification.dto.NotificationDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    public void sendEnrollmentNotification(NotificationDTO notification) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("========================================");
        System.out.println("ENROLLMENT NOTIFICATION");
        System.out.println("========================================");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Student " + notification.getStudentId() + " enrolled into Course " + notification.getCourseId());
        if (notification.getMessage() != null) {
            System.out.println("Message: " + notification.getMessage());
        }
        System.out.println("========================================");
    }
}
