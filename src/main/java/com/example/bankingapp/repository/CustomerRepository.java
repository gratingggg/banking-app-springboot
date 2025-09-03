package com.example.bankingapp.repository;

import com.example.bankingapp.entities.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByAadharNo(String aadharNo);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByPhoneNumber(String phoneNumber);
}
