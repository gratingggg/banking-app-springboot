package com.example.bankingapp.repository;

import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.entities.notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Modifying
    @Query("UPDATE Notification n SET n.notificationStatus = :status WHERE n.customer = :customer AND n.notificationStatus = :currentStatus")
    int markAllAsRead(@Param("status")NotificationStatus status, @Param("customer")Customer customer, @Param("currentStatus") NotificationStatus currentStatus);
}
