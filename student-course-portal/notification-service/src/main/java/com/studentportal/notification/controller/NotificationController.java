package com.studentportal.notification.controller;

import com.studentportal.notification.dto.NotificationDTO;
import com.studentportal.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/enrollment")
    public ResponseEntity<Map<String, String>> sendEnrollmentNotification(@Valid @RequestBody NotificationDTO notification) {
        notificationService.sendEnrollmentNotification(notification);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification sent successfully");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
