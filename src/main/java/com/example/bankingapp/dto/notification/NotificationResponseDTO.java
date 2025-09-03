package com.example.bankingapp.dto.notification;

import com.example.bankingapp.entities.notification.NotificationStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class NotificationResponseDTO {
    private String message;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;

    private NotificationStatus notificationStatus;

    private NotificationType notificationType;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
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
