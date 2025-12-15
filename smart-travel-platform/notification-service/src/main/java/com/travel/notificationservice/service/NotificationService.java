package com.travel.notificationservice.service;

import com.travel.notificationservice.dto.NotificationDTO;
import com.travel.notificationservice.dto.NotificationRequest;
import com.travel.notificationservice.entity.Notification;
import com.travel.notificationservice.exception.NotificationException;
import com.travel.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationDTO sendNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setBookingId(request.getBookingId());
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);
        return mapToDTO(savedNotification);
    }

    public NotificationDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification not found with id: " + id));
        return mapToDTO(notification);
    }

    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getNotificationsByBookingId(Long bookingId) {
        return notificationRepository.findByBookingId(bookingId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getBookingId(),
                notification.getType(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getSentAt()
        );
    }
}
