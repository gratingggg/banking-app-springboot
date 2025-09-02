package com.example.bankingapp.notification;

import com.example.bankingapp.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

enum Status{
    DELIVERED,
    READ,
    UNREAD
}

enum NotificationType{
    ALERT,
    REMINDER,
    OTP
}

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @Column(name = "message")
    @NotBlank(message = "Message cannot be blank.")
    private String message;

    @Column(name = "date")
    @NotNull(message = "Date cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;

    @Column(name = "status")
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "type")
    @NotNull(message = "Notification type cannot be null")
    @Enumerated(EnumType.STRING)
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
