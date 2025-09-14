package com.example.bankingapp.controller;

import com.example.bankingapp.dto.account.AccountRequestDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.account.AccountType;
import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeRole;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                                                 ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.employeeRepository = employeeRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    private Customer createCustomer(int i) {
        Customer customer = new Customer();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword(passwordEncoder.encode("secret" + i));
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + i, 1, 1));
        customer.setAadharNo("1231231231" + i);
        customer.setPhoneNumber("12341234" + i);
        return customer;
    }

    private Account createAccount() {
        Account account = new Account();
        account.setDateOfIssuance(LocalDate.now());
        account.setAccountType(AccountType.CURRENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return account;
    }

    private Employee createEmployee(int i) {
        Employee employee = new Employee();
        employee.setName("Parth " + i + " William");
        employee.setUsername("parth1" + i + "23");
        employee.setPassword(passwordEncoder.encode("secret" + i));
        employee.setEmail("pa" + i + "rth@example.com");
        employee.setGender(PersonGender.MALE);
        employee.setAddress("Mars" + i);
        employee.setDateOfBirth(LocalDate.of(2000 + i, 1, 1));
        employee.setPhoneNumber("12341234" + i);
        employee.setEmployeeRole(EmployeeRole.TELLER);
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        return employee;
    }

    private Transaction createTransaction(int i) {
        Transaction transaction = new Transaction();
        transaction.setLoan(null);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(i * i * 100));
        transaction.setDateOfTransaction(LocalDate.now());

        Employee employee = createEmployee(16);
        transaction.setHandledBy(employee);
        employeeRepository.save(employee);

        return transaction;
    }

    @Test
    public void whenViewAllAccountsOfCustomer_ThenOk() throws Exception {
        Customer customer = createCustomer(44);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(18);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(account.getId()))
                .andExpect(jsonPath("$[0].accountType").value(account.getAccountType().toString()));
    }

    @Test
    public void whenWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(45);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        Customer customer1 = createCustomer(46);

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee/accounts/{accountId}", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/employee/accounts/{accountId}/close", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee/accounts/{accountId}/transactions", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee/accounts/{accountId}/balance", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts/transactions", customer.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee/accounts/{accountId}/deposit", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee/accounts/{accountId}/withdrawl", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenNoEmployeeExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(47);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}", account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(post("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(post("/api/employee/accounts/{accountId}/close", account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/transactions", account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/balance", account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts/transactions", customer.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/deposit", account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/withdrawl", account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."));
    }

    @Test
    public void whenEmployeeNotActive_ThenException() throws Exception {
        Customer customer = createCustomer(48);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(19);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(post("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/close", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/transactions", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/balance", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts/transactions", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/deposit", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/withdrawl", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your account is invalid. Please contact the admin."));
    }

    @Test
    public void whenCustomerDoesNotExist_ThenCustomerNotFound() throws Exception {
        Customer customer = new Customer();
        customer.setId(999999999L);
        Employee employee = createEmployee(20);
        employeeRepository.save(employee);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));

        mockMvc.perform(post("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts/transactions", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    public void whenAccountDoesNotExist_ThenAccountNotFound() throws Exception {
        Account account = new Account();
        account.setId(999999999L);

        Employee employee = createEmployee(21);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/accounts/{accountId}", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));

        mockMvc.perform(post("/api/employee/accounts/{accountId}/close", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/transactions", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/balance", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/deposit", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));

        mockMvc.perform(get("/api/employee/accounts/{accountId}/withdrawl", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));
    }

    @Test
    public void whenViewAllAccountsOfCustomerWithNoAccount_ThenOk() throws Exception{
        Customer customer = createCustomer(44);
        customerRepository.save(customer);

        Employee employee = createEmployee(22);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/{customerId}/accounts", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk());
    }
}
