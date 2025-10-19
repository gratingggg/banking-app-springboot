package com.example.bankingapp;

import com.example.bankingapp.dto.transaction.TransactionRequestDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.account.AccountType;
import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeRole;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.repository.*;
import com.example.bankingapp.specification.NotificationSpecifications;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionTests {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public TransactionTests(MockMvc mockMvc,
                            ObjectMapper objectMapper,
                            PasswordEncoder passwordEncoder,
                            AccountRepository accountRepository,
                            CustomerRepository customerRepository,
                            EmployeeRepository employeeRepository,
                            TransactionRepository transactionRepository,
                            NotificationRepository notificationRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.transactionRepository = transactionRepository;
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

    private Account createAccount() {
        Account account = new Account();
        account.setDateOfIssuance(LocalDate.now());
        account.setAccountType(AccountType.CURRENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return account;
    }

    private Employee createEmployee(int num) {
        String i = "0" + num;
        if (num >= 100) i = "" + num;
        Employee employee = new Employee();
        employee.setName("Parth " + i + " William");
        employee.setUsername("parth1" + i + "23");
        employee.setPassword(passwordEncoder.encode("secret" + i));
        employee.setEmail("pa" + i + "rth@example.com");
        employee.setGender(PersonGender.MALE);
        employee.setAddress("Mars" + i);
        employee.setDateOfBirth(LocalDate.of(2000 + num, 1, 1));
        employee.setPhoneNumber("1234123" + i);
        employee.setEmployeeRole(EmployeeRole.TELLER);
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        return employee;
    }

    private Transaction createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setLoan(null);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(299));
        transaction.setDateOfTransaction(LocalDateTime.now());
        transaction.setHandledBy(null);
        transaction.setFailureReason(null);

        return transaction;
    }

    private TransactionRequestDTO createTransactionRequestDTO(Account fromAccount, Account toAccount) {
        TransactionRequestDTO requestDTO = new TransactionRequestDTO();
        requestDTO.setAmount(BigDecimal.valueOf(2000));
        requestDTO.setTransactionType(TransactionType.TRANSFERRED);
        requestDTO.setFromAccountId(fromAccount.getId());
        requestDTO.setToAccountId(toAccount.getId());

        return requestDTO;
    }

    @Test
    public void whenTransferFund_ThenOk() throws Exception {
        Customer fromCustomer = createCustomer(114);
        Customer toCustomer = createCustomer(115);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("$.amount").value(requestDTO.getAmount().toString()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.transactionType").value(requestDTO.getTransactionType().toString()))
                .andExpect(jsonPath("$.failureReason").doesNotExist());

        Specification<Notification> fromSpec = NotificationSpecifications.forCustomer(fromCustomer);
        List<Notification> fromNotifications = notificationRepository.findAll(fromSpec);
        Specification<Notification> toSpec = NotificationSpecifications.forCustomer(toCustomer);
        List<Notification> toNotifications = notificationRepository.findAll(toSpec);

        assertEquals(1, fromNotifications.size());
        assertEquals(1, toNotifications.size());
    }

    @Test
    public void whenTransferFund_InvalidCustomer_ThenNotFound() throws Exception {
        Customer toCustomer = createCustomer(116);
        Account fromAccount = createAccount();
        Account toAccount = createAccount();
        toCustomer.addAccount(toAccount);
        customerRepository.save(toCustomer);
        accountRepository.save(toAccount);

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user("IDoNotExist").roles(toCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The customer with username IDoNotExist not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFund_InvalidSourceAccount_ThenNotFound() throws Exception {
        Customer fromCustomer = createCustomer(117);
        Customer toCustomer = createCustomer(118);
        Account fromAccount = createAccount();
        fromAccount.setId(99999999L);
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.save(toAccount);

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with id " + fromAccount.getId() + " does not exist"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFund_InvalidDestinationAccount_ThenNotFound() throws Exception {
        Customer fromCustomer = createCustomer(119);
        Customer toCustomer = createCustomer(120);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        toAccount.setId(999999999999L);
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.save(fromAccount);

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with id " + toAccount.getId() + " does not exist"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFund_UnauthorizedCustomer_ThenForbidden() throws Exception {
        Customer fromCustomer = createCustomer(121);
        Customer toCustomer = createCustomer(122);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        Customer customer = createCustomer(123);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer, customer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(("You are trying to access someone else's transaction.")))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenTransferFund_InactiveSourceAccount_ThenBadRequest() throws Exception {
        Customer fromCustomer = createCustomer(124);
        Customer toCustomer = createCustomer(125);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        fromAccount.setAccountStatus(AccountStatus.INACTIVE);
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(("Your account is currently " + fromAccount.getAccountStatus() + ". Please contact the admin.")))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFund_InactiveDestinationAccount_ThenBadRequest() throws Exception {
        Customer fromCustomer = createCustomer(126);
        Customer toCustomer = createCustomer(127);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        toAccount.setAccountStatus(AccountStatus.INACTIVE);
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(("The recipient's account is currently " + toAccount.getAccountStatus() + ".")))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFund_SameAccount_ThenBadRequest() throws Exception {
        Customer fromCustomer = createCustomer(128);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        fromCustomer.addAccount(fromAccount);
        customerRepository.save(fromCustomer);
        accountRepository.save(fromAccount);

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, fromAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(("You cannot transfer between the same accounts.")))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFund_InvalidAmount_ThenBadRequest() throws Exception {
        Customer fromCustomer = createCustomer(129);
        Customer toCustomer = createCustomer(130);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        requestDTO.setAmount(BigDecimal.valueOf(-345));
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(("Entered amount is invalid. Please enter a positive amount.")))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFund_InsufficientBalance_ThenFailure() throws Exception {
        Customer fromCustomer = createCustomer(131);
        Customer toCustomer = createCustomer(132);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(1000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("$.failureReason").value(("Insufficient balance.")))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()));
    }

    @Test
    public void whenTransferFund_LimitReached_ThenFailure() throws Exception {
        Customer fromCustomer = createCustomer(133);
        Customer toCustomer = createCustomer(134);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        Transaction transaction = createTransaction();
        transaction.setAmount(BigDecimal.valueOf(50000));
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));
        transactionRepository.save(transaction);

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/transactions/transfer")
                        .with(user(fromCustomer.getUsername()).roles(fromCustomer.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("$.failureReason").value(("Daily payment limit reached.")))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()));
    }

    @Test
    public void whenGetTransaction_ThenOk() throws Exception {
        Customer customer = createCustomer(135);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        mockMvc.perform(get("/api/customer/transactions/" + transaction.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transaction.getId()))
                .andExpect(jsonPath("$.fromAccountId").value(account.getId()))
                .andExpect(jsonPath("transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.transactionType").value(transaction.getTransactionType().toString()));
    }

    @Test
    public void whenGetTransaction_InvalidCustomer_ThenNotFound() throws Exception {
        Customer customer = new Customer();
        customer.setId(99999999L);
        customer.setUsername("IDoNotExist");

        mockMvc.perform(get("/api/customer/transactions/" + 23487265987345L)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The customer with username " + customer.getUsername() + " not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetTransaction_InvalidTransaction_ThenNotFound() throws Exception {
        Customer customer = createCustomer(136);
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customer/transactions/" + 23487265987345L)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetTransaction_UnauthorizedCustomer_ThenForbidden() throws Exception {
        Customer fromCustomer = createCustomer(137);
        Customer toCustomer = createCustomer(138);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        Transaction transaction = createTransaction();
        transaction.setTransactionType(TransactionType.TRANSFERRED);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        Customer customer = createCustomer(139);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer, customer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));
        transactionRepository.save(transaction);

        mockMvc.perform(get("/api/customer/transactions/" + transaction.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are trying to access someone else's transaction."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenGetTransactionByEmployee_ThenOk() throws Exception {
        Customer fromCustomer = createCustomer(140);
        Customer toCustomer = createCustomer(141);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        Transaction transaction = createTransaction();
        transaction.setTransactionType(TransactionType.TRANSFERRED);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));
        transactionRepository.save(transaction);

        Employee employee = createEmployee(65);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/transactions/" + transaction.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transaction.getId()))
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.transactionType").value(transaction.getTransactionType().toString()));
    }

    @Test
    public void whenGetTransactionByEmployee_WrongRole_ThenForbidden() throws Exception {
        mockMvc.perform(get("/api/employee/transactions/1")
                        .with(user("IAmWrongRole").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenGetTransactionByEmployee_InvalidEmployee_ThenNotFound() throws Exception {
        mockMvc.perform(get("/api/employee/transactions/1")
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with username IDoNotExist not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetTransactionByEmployee_InactiveEmployee_ThenUnauthorized() throws Exception {
        Employee employee = createEmployee(66);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/transactions/3")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenGetTransactionByEmployee_InvalidTransaction_ThenNotFound() throws Exception {
        Employee employee = createEmployee(67);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/transactions/999999999")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFundByEmployee_ThenOk() throws Exception {
        Customer fromCustomer = createCustomer(142);
        Customer toCustomer = createCustomer(143);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(68);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("$.amount").value(requestDTO.getAmount().toString()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.transactionType").value(requestDTO.getTransactionType().toString()))
                .andExpect(jsonPath("$.handledBy").value(employee.getName()))
                .andExpect(jsonPath("$.failureReason").doesNotExist());

        Specification<Notification> fromSpec = NotificationSpecifications.forCustomer(fromCustomer);
        List<Notification> fromNotifications = notificationRepository.findAll(fromSpec);
        Specification<Notification> toSpec = NotificationSpecifications.forCustomer(toCustomer);
        List<Notification> toNotifications = notificationRepository.findAll(toSpec);

        assertEquals(1, fromNotifications.size());
        assertEquals(1, toNotifications.size());
    }

    @Test
    public void whenTransferFundByEmployee_WrongRole_ThenForbidden() throws Exception {
        Customer fromCustomer = createCustomer(144);
        Customer toCustomer = createCustomer(145);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user("IAmWrongRole").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenTransferFundByEmployee_InvalidEmployee_ThenNotFound() throws Exception {
        Customer fromCustomer = createCustomer(146);
        Customer toCustomer = createCustomer(147);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user("IDoNotExist").roles("EMPLOYEE"))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with username IDoNotExist not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFund_InactiveEmployee_ThenUnauthorized() throws Exception {
        Customer fromCustomer = createCustomer(148);
        Customer toCustomer = createCustomer(149);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(69);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenTransferFundByEmployee_InvalidSourceAccount_ThenNotFound() throws Exception {
        Customer fromCustomer = createCustomer(150);
        Customer toCustomer = createCustomer(151);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        fromAccount.setId(99999999999L);
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(70);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with id " + fromAccount.getId() + " does not exist"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFundByEmployee_InvalidDestinationAccount_ThenNotFound() throws Exception {
        Customer fromCustomer = createCustomer(152);
        Customer toCustomer = createCustomer(153);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        toAccount.setId(999999999L);
        fromCustomer.addAccount(fromAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(71);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with id " + toAccount.getId() + " does not exist"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenTransferFundByEmployee_InactiveSourceAccount_ThenBadRequest() throws Exception{
        Customer fromCustomer = createCustomer(154);
        Customer toCustomer = createCustomer(155);
        Account fromAccount = createAccount();
        fromAccount.setAccountStatus(AccountStatus.INACTIVE);
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(72);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your account is currently " + fromAccount.getAccountStatus() + ". Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFundByEmployee_InactiveDestinationAccount_ThenBadRequest() throws Exception{
        Customer fromCustomer = createCustomer(156);
        Customer toCustomer = createCustomer(157);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        toAccount.setAccountStatus(AccountStatus.INACTIVE);
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(73);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The recipient's account is currently " + toAccount.getAccountStatus() + "."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFundByEmployee_SameAccount_ThenBadRequest() throws Exception{
        Customer fromCustomer = createCustomer(158);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        fromCustomer.addAccount(fromAccount);
        customerRepository.saveAll(List.of(fromCustomer));
        accountRepository.saveAll(List.of(fromAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, fromAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(74);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot transfer between the same accounts."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFundByEmployee_InvalidAmount_ThenBadRequest() throws Exception{
        Customer fromCustomer = createCustomer(159);
        Customer toCustomer = createCustomer(160);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        requestDTO.setAmount(BigDecimal.valueOf(-299));
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(75);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Entered amount is invalid. Please enter a positive amount."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenTransferFundByEmployee_InsufficientBalance_ThenFailure() throws Exception{
        Customer fromCustomer = createCustomer(161);
        Customer toCustomer = createCustomer(162);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(1000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(76);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("$.failureReason").value(("Insufficient balance.")))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()))
                .andExpect(jsonPath("$.handledBy").value(employee.getName()));
    }

    @Test
    public void whenTransferFundByEmployee_LimitReached_ThenFailure() throws Exception{
        Customer fromCustomer = createCustomer(163);
        Customer toCustomer = createCustomer(164);
        Account fromAccount = createAccount();
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        Account toAccount = createAccount();
        fromCustomer.addAccount(fromAccount);
        toCustomer.addAccount(toAccount);
        Transaction transaction = createTransaction();
        transaction.setAmount(BigDecimal.valueOf(50000));
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        customerRepository.saveAll(List.of(fromCustomer, toCustomer));
        accountRepository.saveAll(List.of(fromAccount, toAccount));
        transactionRepository.save(transaction);

        TransactionRequestDTO requestDTO = createTransactionRequestDTO(fromAccount, toAccount);
        String request = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(77);
        employeeRepository.save(employee);

        mockMvc.perform(post("/api/employee/transactions/transfer")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.toAccountId").value(toAccount.getId()))
                .andExpect(jsonPath("$.failureReason").value(("Daily payment limit reached.")))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()))
                .andExpect(jsonPath("$.handledBy").value(employee.getName()));
    }
}