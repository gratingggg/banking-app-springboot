package com.example.bankingapp.service;

import com.example.bankingapp.dto.notification.NotificationResponseDTO;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.entities.notification.NotificationStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.exception.CustomerNotFoundException;
import com.example.bankingapp.exception.NotificationAccessDeniedException;
import com.example.bankingapp.exception.NotificationNotFoundException;
import com.example.bankingapp.repository.CustomerRepository;
import com.example.bankingapp.repository.NotificationRepository;
import com.example.bankingapp.specification.NotificationSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               CustomerRepository customerRepository){
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
    }

    private NotificationResponseDTO notificationToDTO(Notification notification){
        NotificationResponseDTO responseDTO = new NotificationResponseDTO();
        responseDTO.setNotificationId(notification.getId());
        responseDTO.setDate(notification.getDate());
        responseDTO.setNotificationStatus(notification.getNotificationStatus());
        responseDTO.setNotificationType(notification.getNotificationType());
        responseDTO.setMessage(notification.getMessage());

        return responseDTO;
    }

    public Page<NotificationResponseDTO> getAllNotifications(int page, int size, NotificationStatus status,
                                                             NotificationType type, LocalDate fromDate,
                                                             LocalDate toDate, String username){
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new CustomerNotFoundException("The customer with the username " + username + " not found."));
        Specification<Notification> specification = NotificationSpecifications.forCustomer(customer)
                .and(NotificationSpecifications.withStatus(status))
                .and(NotificationSpecifications.withType(type))
                .and(NotificationSpecifications.withDate(fromDate, toDate));
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Notification> notifications = notificationRepository.findAll(specification, pageable);

        return notifications.map(this::notificationToDTO);
    }

    public NotificationResponseDTO getNotification(Long notificationId, String username){
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new CustomerNotFoundException("The customer with the username " + username + " not found."));
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);
        if(!notification.getCustomer().equals(customer)){
            throw new NotificationAccessDeniedException();
        }
        if(notification.isUnread()){
            notification.setNotificationStatus(NotificationStatus.READ);
        }
        notificationRepository.save(notification);

        return notificationToDTO(notification);
    }

    public void readAllNotifications(String username){
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new CustomerNotFoundException("The customer with the username " + username + " not found."));
        notificationRepository.markAllAsRead(NotificationStatus.UNREAD, customer, NotificationStatus.READ);
    }

    public void createNotification(Customer customer, NotificationType type, String message){
        Notification notification = new Notification();
        notification.setDate(LocalDateTime.now());
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setNotificationStatus(NotificationStatus.UNREAD);
        notification.setCustomer(customer);

        notificationRepository.save(notification);
    }
}
