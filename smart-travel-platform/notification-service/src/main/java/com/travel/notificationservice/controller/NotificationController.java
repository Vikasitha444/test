package com.travel.notificationservice.controller;

import com.travel.notificationservice.dto.ApiResponse;
import com.travel.notificationservice.dto.NotificationDTO;
import com.travel.notificationservice.dto.NotificationRequest;
import com.travel.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> sendNotification(@RequestBody NotificationRequest request) {
        NotificationDTO notification = notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification sent successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotificationById(@PathVariable Long id) {
        NotificationDTO notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByBookingId(@PathVariable Long bookingId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }
}
