package com.example.bankingapp.entities.notification;

import com.example.bankingapp.entities.baseentities.BaseEntity;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.utils.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @Column(name = "message")
    @NotBlank(message = "Message cannot be blank.")
    private String message;

    @Column(name = "date")
    @NotNull(message = "Date cannot be null")
    @DateTimeFormat(pattern = Constants.LocalDateTimePattern)
    private LocalDateTime date;

    @Column(name = "status")
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(name = "type")
    @NotNull(message = "Notification type cannot be null")
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public void markAsRead(){
        notificationStatus = NotificationStatus.READ;
    }

    public void markAsUnread(){
        notificationStatus = NotificationStatus.UNREAD;
    }

    public boolean isUnread(){
        return notificationStatus == NotificationStatus.UNREAD;
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("id : ", this.getId())
                .append("message : ", getMessage())
                .append("type : ", getNotificationType())
                .append("status : ", getNotificationStatus())
                .append("date : ", getDate())
                .toString();
    }
}
