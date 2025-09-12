package com.example.bankingapp.controller;

import com.example.bankingapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeCustomerAccountControllerTest {
    private final MockMvc mockMvc;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmployeeRepository employeeRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeCustomerAccountControllerTest(MockMvc mockMvc,
                                         CustomerRepository customerRepository,
                                         AccountRepository accountRepository,
                                         TransactionRepository transactionRepository,
                                         EmployeeRepository employeeRepository,
                                         LoanRepository loanRepository,
                                         PasswordEncoder passwordEncoder,
                                         ObjectMapper objectMapper){
        this.mockMvc = mockMvc;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.employeeRepository = employeeRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }


}
