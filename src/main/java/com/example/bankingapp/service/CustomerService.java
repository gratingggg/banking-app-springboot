package com.example.bankingapp.service;

import com.example.bankingapp.dto.customer.CustomerLoginDTO;
import com.example.bankingapp.dto.customer.CustomerRequestDTO;
import com.example.bankingapp.dto.customer.CustomerResponseDTO;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.exception.*;
import com.example.bankingapp.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                           NotificationService notificationService) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    private void setBasicFields(Customer customer, CustomerRequestDTO customerRequestDTO) {
        customer.setName(customerRequestDTO.getName());
        customer.setDateOfBirth(customerRequestDTO.getDateOfBirth());
        customer.setAddress(customerRequestDTO.getAddress());
        customer.setGender(customerRequestDTO.getGender());
    }

    private void setUniqueFields(Customer customer, CustomerRequestDTO customerRequestDTO) {
        if (customerRequestDTO.getEmail() != null) {
            if (customerRepository.findByEmail(customerRequestDTO.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException("The email " + customerRequestDTO.getEmail() + " is already registered. Please enter another email.");
            }
            customer.setEmail(customerRequestDTO.getEmail());
        }

        if (customerRequestDTO.getPhoneNumber() != null) {
            if (customerRepository.findByPhoneNumber(customerRequestDTO.getPhoneNumber()).isPresent()) {
                throw new PhoneNumberAlreadyExistsException("The phone number " + customerRequestDTO.getPhoneNumber() + " is already registered. Please enter another phone number.");
            }
            customer.setPhoneNumber(customerRequestDTO.getPhoneNumber());
        }

        if (customerRequestDTO.getAadharNo() != null) {
            if (customerRepository.findByAadharNo(customerRequestDTO.getAadharNo()).isPresent()) {
                throw new AadharNumberAlreadyExistsException("The aadhar number " + customerRequestDTO.getAadharNo() + " is already registered. Please enter another aadhar number.");
            }
            customer.setAadharNo(customerRequestDTO.getAadharNo());
        }

        if (customerRequestDTO.getUsername() != null) {
            if (customerRepository.findByUsername(customerRequestDTO.getUsername()).isPresent()) {
                throw new UsernameAlreadyExistsException("The username " + customerRequestDTO.getUsername() + " is already taken. Please choose another username.");
            }
            customer.setUsername(customerRequestDTO.getUsername());
        }
    }

    private CustomerResponseDTO mapCustomerToDTO(Customer customer) {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setAddress(customer.getAddress());
        customerResponseDTO.setEmail(customer.getEmail());
        customerResponseDTO.setGender(customer.getGender());
        customerResponseDTO.setName(customer.getName());
        customerResponseDTO.setUsername(customer.getUsername());
        customerResponseDTO.setPhoneNumber(customer.getPhoneNumber());
        customerResponseDTO.setDateOfBirth(customer.getDateOfBirth());

        return customerResponseDTO;
    }

    @Transactional
    public CustomerResponseDTO processCustomerRegistration(CustomerRequestDTO customerRequestDTO) {
        Customer customer = new Customer();

        setBasicFields(customer, customerRequestDTO);
        setUniqueFields(customer, customerRequestDTO);
        if (customerRequestDTO.getPassword() != null) {
            customer.setPassword(passwordEncoder.encode(customerRequestDTO.getPassword()));
        }

        customerRepository.save(customer);
        String message = "Welcome " + customer.getName() + "! Your registration was successful." +
                "You can now log in and start managing your banking activities securely.";
        notificationService.createNotification(customer, NotificationType.INFO, message);

        return mapCustomerToDTO(customer);
    }

    public CustomerResponseDTO processCustomerLogin(CustomerLoginDTO customerLoginDTO) {
        Customer customer = customerRepository.findByUsername(customerLoginDTO.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("The customer with the username " + customerLoginDTO.getUsername() + " does not exist." +
                        "Please enter the valid username."));

        if (passwordEncoder.matches(customerLoginDTO.getPassword(), customer.getPassword())) {
            return mapCustomerToDTO(customer);
        } else throw new InvalidCredentialsException("Password do not match. Please enter the correct password.");
    }
}
