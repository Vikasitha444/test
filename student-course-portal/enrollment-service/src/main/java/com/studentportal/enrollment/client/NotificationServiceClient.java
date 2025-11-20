package com.studentportal.enrollment.client;

import com.studentportal.enrollment.dto.NotificationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    public NotificationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEnrollmentNotification(Long studentId, Long courseId) {
        try {
            String url = notificationServiceUrl + "/notify/enrollment";
            NotificationDTO notification = new NotificationDTO(studentId, courseId, "Enrollment successful");
            restTemplate.postForEntity(url, notification, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
