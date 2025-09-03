package com.example.bankingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankingapp.entities.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
