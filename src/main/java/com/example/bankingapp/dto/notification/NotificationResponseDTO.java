package com.example.bankingapp.dto.notification;

import com.example.bankingapp.entities.notification.NotificationStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class NotificationResponseDTO {

    private Long notificationId;

    private String message;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime date;

    private NotificationStatus notificationStatus;

    private NotificationType notificationType;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
