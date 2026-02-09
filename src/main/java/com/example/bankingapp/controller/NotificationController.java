package com.example.bankingapp.controller;

import com.example.bankingapp.dto.notification.NotificationResponseDTO;
import com.example.bankingapp.entities.notification.NotificationStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.service.NotificationService;
import com.example.bankingapp.utils.Endpoints;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @GetMapping(Endpoints.NOTIFICATIONS_ALL)
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(@RequestParam(required = false, defaultValue = "0") int page,
                                                                             @RequestParam(required = false, defaultValue = "10") int size,
                                                                             @RequestParam(required = false) NotificationType type,
                                                                             @RequestParam(required = false) LocalDate fromDate,
                                                                             @RequestParam(required = false) LocalDate toDate,
                                                                             @RequestParam(required = false) NotificationStatus status,
                                                                             Principal principal){
        Page<NotificationResponseDTO> notifications = notificationService.getAllNotifications(page, size, status, type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping(Endpoints.NOTIFICATION_PARTICULAR)
    public ResponseEntity<NotificationResponseDTO> getNotification(@PathVariable Long notificationId, Principal principal){
        NotificationResponseDTO responseDTO = notificationService.getNotification(notificationId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping(Endpoints.NOTIFICATION_READ_ALL)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAllNotifications(Principal principal){
        notificationService.readAllNotifications(principal.getName());
    }
}
