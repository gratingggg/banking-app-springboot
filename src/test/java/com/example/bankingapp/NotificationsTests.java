package com.example.bankingapp;

import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.entities.notification.NotificationStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.repository.CustomerRepository;
import com.example.bankingapp.repository.NotificationRepository;
import com.example.bankingapp.service.NotificationService;
import com.example.bankingapp.specification.NotificationSpecifications;
import com.example.bankingapp.utils.Endpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationsTests{
    private final MockMvc mockMvc;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationsTests(MockMvc mockMvc,
                              PasswordEncoder passwordEncoder,
                              CustomerRepository customerRepository,
                              NotificationService notificationService,
                              NotificationRepository notificationRepository){
        this.mockMvc = mockMvc;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    private Customer createCustomer(int num) {
        String i = "" + num;
        if (num < 100) i = "0" + num;
        Customer customer = new Customer();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword(passwordEncoder.encode("secret" + i));
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + num, 1, 1));
        customer.setAadharNo("123123123" + i);
        customer.setPhoneNumber("1234123" + i);
        return customer;
    }

    private Notification createNotification(Customer customer, String message){
        Notification notification = new Notification();
        notification.setDate(LocalDateTime.now());
        notification.setMessage(message);
        notification.setNotificationType(NotificationType.INFO);
        notification.setNotificationStatus(NotificationStatus.UNREAD);

        customer.addNotification(notification);

        return notification;
    }

    @Test
    public void whenGetAllNotifications_ThenOk() throws Exception{
        Customer customer = createCustomer(165);
        customerRepository.save(customer);
        for(int i = 0; i < 30; i++){
            String message = "Hi my name is " + i*i*i*i + ".";
            NotificationType type = NotificationType.INFO;
            if(i % 2 == 0)
                type = NotificationType.TRANSACTION;
            if(i % 3 == 0)
                type = NotificationType.ALERT;

            notificationService.createNotification(customer, type, message);
        }

        mockMvc.perform(get(Endpoints.NOTIFICATIONS_ALL)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .param("page", "2")
                        .param("size", "3")
                        .param("type", "TRANSACTION")
                        .param("fromDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content.[0].notificationId").exists())
                .andExpect(jsonPath("$.content.[0].notificationType").value(NotificationType.TRANSACTION.toString()))
                .andExpect(jsonPath("$.content.[1].notificationId").exists())
                .andExpect(jsonPath("$.content.[1].notificationType").value(NotificationType.TRANSACTION.toString()))
                .andExpect(jsonPath("$.content.[2].notificationId").exists())
                .andExpect(jsonPath("$.content.[2].notificationType").value(NotificationType.TRANSACTION.toString()));
    }

    @Test
    public void whenGetAllNotifications_ThenEmptyNotifications() throws Exception{
        Customer customer = createCustomer(166);
        customerRepository.save(customer);

        mockMvc.perform(get(Endpoints.NOTIFICATIONS_ALL)
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    public void whenGetAllNotifications_InvalidCustomer_CustomerNotFound() throws Exception{
        mockMvc.perform(get(Endpoints.NOTIFICATIONS_ALL)
                .with((user("IDoNotExist").roles("CUSTOMER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The customer with the username IDoNotExist not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetNotification_ThenOk() throws Exception{
        Customer customer = createCustomer(167);
        customerRepository.save(customer);
        Notification notification = createNotification(customer, "Hey");
        notificationRepository.save(notification);

        mockMvc.perform(get(Endpoints.NOTIFICATION_PARTICULAR, notification.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(notification.getMessage()))
                .andExpect(jsonPath("$.notificationStatus").value(NotificationStatus.READ.toString()))
                .andExpect(jsonPath("$.notificationType").value(notification.getNotificationType().toString()));
    }

    @Test
    public void whenGetNotification_InvalidCustomer_ThenNotFound() throws Exception{
        mockMvc.perform(get(Endpoints.NOTIFICATION_PARTICULAR, 1)
                .with(user("IDoNotExist").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The customer with the username IDoNotExist not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetNotification_InvalidNotification_ThenNotFound() throws Exception{
        Customer customer = createCustomer(168);
        customerRepository.save(customer);

        mockMvc.perform(get(Endpoints.NOTIFICATION_PARTICULAR ,99999999999L)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notification not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetNotification_UnauthorizedCustomer_ThenForbidden() throws Exception{
        Customer customer1 = createCustomer(169);
        Customer customer = createCustomer(170);
        customerRepository.saveAll(List.of(customer, customer1));
        Notification notification = createNotification(customer1, "Hey");
        notificationRepository.save(notification);

        mockMvc.perform(get(Endpoints.NOTIFICATION_PARTICULAR, notification.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are trying to view someone else's notifications"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenReadAllNotifications_ThenOk() throws Exception{
        Customer customer = createCustomer(171);
        customerRepository.save(customer);
        for(int i = 0; i < 10; i++){
            notificationService.createNotification(customer, NotificationType.INFO, "Hey, " + i*i*i*10);
        }
        Specification<Notification> specification = NotificationSpecifications.forCustomer(customer);
        List<Notification> list = notificationRepository.findAll(specification);
        assertEquals(10, list.stream().filter(Notification::isUnread).count());

        mockMvc.perform(put(Endpoints.NOTIFICATION_READ_ALL)
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNoContent());

        List<Notification> updatedList = notificationRepository.findAll(specification);
        updatedList.forEach(notification -> assertEquals(NotificationStatus.READ, notification.getNotificationStatus()));
    }

    @Test
    public void whenReadAllNotifications_InvalidCustomer_ThenNotFound() throws Exception{
        mockMvc.perform(put(Endpoints.NOTIFICATION_READ_ALL)
                .with(user("IDoNotExist").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The customer with the username IDoNotExist not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));

    }
}
