package com.example.bankingapp;

import com.example.bankingapp.dto.loan.LoanRepaymentDTO;
import com.example.bankingapp.dto.loan.LoanRequestDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.account.AccountType;
import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeRole;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LoanTests {
    private final int COUNT = 172;
    private final int EMP = 78;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;


    @Autowired
    public LoanTests(MockMvc mockMvc,
                     ObjectMapper objectMapper,
                     LoanRepository loanRepository,
                     PasswordEncoder passwordEncoder,
                     AccountRepository accountRepository,
                     CustomerRepository customerRepository,
                     EmployeeRepository employeeRepository,
                     TransactionRepository transactionRepository,
                     NotificationRepository notificationRepository){
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.loanRepository = loanRepository;
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
        account.setBalance(BigDecimal.valueOf(200000));
        return account;
    }

    private LoanRequestDTO createLoanRequest(){
        LoanRequestDTO requestDTO = new LoanRequestDTO();
        requestDTO.setLoanType(LoanType.GOLD);
        requestDTO.setPrincipalAmount(BigDecimal.valueOf(2000));
        requestDTO.setTenureInMonths(20);
        return requestDTO;
    }

    private Loan createLoan(){
        Loan loan = new Loan();
        loan.setLoanStatus(LoanStatus.DISBURSED);
        loan.setLoanType(LoanType.BUSINESS);
        loan.setTenureInMonths(60);
        loan.setPrincipalAmount(BigDecimal.valueOf(2000));
        loan.setRateOfInterest(BigDecimal.valueOf(13));
        loan.setDateOfIssuance(LocalDate.of(2024, 12, 12));

        return loan;
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

    @Test
    public void whenCreateLoan_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").exists())
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.tenureInMonths").value(requestDTO.getTenureInMonths()))
                .andExpect(jsonPath("$.loanType").value(requestDTO.getLoanType().toString()))
                .andExpect(jsonPath("$.loanStatus").value(LoanStatus.PENDING.toString()))
                .andExpect(jsonPath("$.principalAmount").value(requestDTO.getPrincipalAmount().toString()));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenCreateLoan_NoCustomer_ThenNotFound() throws Exception{
        Customer customer = new Customer();
        customer.setUsername("IDoNotExist");

        LoanRequestDTO requestDTO = createLoanRequest();
        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateLoan_NoAccount_ThenNotFound() throws Exception{
        Customer customer = createCustomer(COUNT + 1);
        Account account = new Account();
        account.setId(99999999999L);
        customerRepository.save(customer);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateLoan_OthersAccount_ThenForbidden() throws Exception{
        Customer customer1 = createCustomer(COUNT + 2);
        Customer customer2 = createCustomer(COUNT + 3);
        Account account = createAccount();
        customer1.addAccount(account);
        customerRepository.saveAll(List.of(customer1, customer2));
        accountRepository.save(account);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                        .with(user(customer2.getUsername()).roles(customer2.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account access denied."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenCreateLoan_InactiveAccount_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 4);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.INACTIVE);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not active."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateLoan_NonPositiveTenure_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 5);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());
        requestDTO.setTenureInMonths(-10);

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tenure in months cannot be non-positive."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateLoan_NonPositiveLoan_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 6);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());
        requestDTO.setPrincipalAmount(BigDecimal.valueOf(-10));

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Entered amount is invalid. Please enter a positive amount."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateLoan_OverdueLoan_ThenConflict() throws Exception {
        Customer customer = createCustomer(COUNT + 7);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = createLoan();
        loan.setDateOfIssuance(LocalDate.of(2000, 12, 31));
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/loan/apply")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You have an overdue loan! Cannot create a new loan."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void whenGetAllLoans_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 8);
        Account account = createAccount();
        customer.addAccount(account);
        List<Loan> loans = new ArrayList<>();

        for(int i = 0; i < 30; i++){
            Loan loan = createLoan();
            if(i % 5 == 0) loan.setLoanType(LoanType.BUSINESS);
            else if(i % 5 == 1) loan.setLoanType(LoanType.VEHICLE);
            else if(i % 5 == 2) loan.setLoanType(LoanType.GOLD);
            else if(i % 5 == 3) loan.setLoanType(LoanType.EDUCATION);
            else loan.setLoanType(LoanType.HOME);

            if(i % 4 == 0) loan.setLoanStatus(LoanStatus.PENDING);
            else if(i % 4 == 1) loan.setLoanStatus(LoanStatus.APPROVED);
            else if(i % 4 == 2) loan.setLoanStatus(LoanStatus.DISBURSED);
            else loan.setLoanStatus(LoanStatus.CLOSED);

            account.addLoan(loan);
            loans.add(loan);
        }

        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.saveAll(loans);

        mockMvc.perform(get("/api/customer/loan")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.content[0].loanType").exists())
                .andExpect(jsonPath("$.content[0].loanStatus").exists());
    }

    @Test
    public void whenGetAllLoans_WithFilters_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 9);
        Account account = createAccount();
        customer.addAccount(account);
        List<Loan> loans = new ArrayList<>();

        for(int i = 0; i < 30; i++){
            Loan loan = createLoan();
            if(i % 5 == 0) loan.setLoanType(LoanType.BUSINESS);
            else if(i % 5 == 1) loan.setLoanType(LoanType.VEHICLE);
            else if(i % 5 == 2) loan.setLoanType(LoanType.GOLD);
            else if(i % 5 == 3) loan.setLoanType(LoanType.EDUCATION);
            else loan.setLoanType(LoanType.HOME);

            if(i % 4 == 0) loan.setLoanStatus(LoanStatus.PENDING);
            else if(i % 4 == 1) loan.setLoanStatus(LoanStatus.APPROVED);
            else if(i % 4 == 2) loan.setLoanStatus(LoanStatus.DISBURSED);
            else loan.setLoanStatus(LoanStatus.CLOSED);

            account.addLoan(loan);
            loans.add(loan);
        }

        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.saveAll(loans);

        mockMvc.perform(get("/api/customer/loan")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .param("page", "1")
                        .param("size", "3")
                        .param("type", LoanType.GOLD.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].loanType").value(LoanType.GOLD.toString()))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(jsonPath("$.pageable.pageSize").value(3));
    }

    @Test
    public void whenRepayLoan_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 10);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = createLoan();
        loan.setDateOfIssuance(LocalDate.now().minusMonths(1));
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRepaymentDTO repay = new LoanRepaymentDTO();
        repay.setAmount(loan.calculateEMI());
        repay.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repay);

        mockMvc.perform(post("/api/customer/loan/repay")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.amount").value(repay.getAmount().toString()))
                .andExpect(jsonPath("$.fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.loanId").value(loan.getId()))
                .andExpect(jsonPath("$.failureReason").isEmpty())
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.transactionType").value(TransactionType.LOAN_REPAYMENT.toString()));

        Account newAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(0, account.getBalance().subtract(repay.getAmount()).compareTo(newAccount.getBalance()));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenRepayLoan_NoCustomer_ThenNotFound() throws Exception{
        Customer customer = new Customer();
        customer.setId(9999999999L);
        customer.setUsername("IDoNotExist.");

        String body = objectMapper.writeValueAsString(new LoanRepaymentDTO());

        mockMvc.perform(post("/api/customer/loan/repay")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenRepayLoan_NoLoan_ThenNotFound() throws Exception{
        Customer customer = createCustomer(COUNT + 11);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = new Loan();
        loan.setId(999999999999999999L);
        customerRepository.save(customer);
        accountRepository.save(account);

        LoanRepaymentDTO repaymentDTO = new LoanRepaymentDTO();
        repaymentDTO.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repaymentDTO);

        mockMvc.perform(post("/api/customer/loan/repay")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Loan not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenRepayLoan_OtherCustomer_ThenNotFound() throws Exception{
        Customer customer1 = createCustomer(COUNT + 12);
        Customer customer = createCustomer(COUNT + 13);
        Account account = createAccount();
        customer1.addAccount(account);
        Loan loan = createLoan();
        account.addLoan(loan);
        customerRepository.saveAll(List.of(customer1, customer));
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRepaymentDTO repay = new LoanRepaymentDTO();
        repay.setAmount(loan.calculateEMI().multiply(BigDecimal.TEN));
        repay.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repay);

        mockMvc.perform(post("/api/customer/loan/repay")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Loan access denied."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenRepayLoan_NotDisbursed_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 14);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = createLoan();
        loan.setLoanStatus(LoanStatus.APPROVED);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRepaymentDTO repay = new LoanRepaymentDTO();
        repay.setAmount(loan.calculateEMI().multiply(BigDecimal.TEN));
        repay.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repay);

        mockMvc.perform(post("/api/customer/loan/repay")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Loan not disbursed yet."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenRepayLoan_AmountInvalid_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 15);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = createLoan();
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRepaymentDTO repay = new LoanRepaymentDTO();
        repay.setAmount(loan.calculateEMI().multiply(BigDecimal.valueOf(-1)));
        repay.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repay);

        mockMvc.perform(post("/api/customer/loan/repay")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Repayment amount cannot be negative."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenRepayLoan_InsufficientBalance_ThenTransactionFailure() throws Exception{
        Customer customer = createCustomer(COUNT + 16);
        Account account = createAccount();
        account.setBalance(BigDecimal.ZERO);
        customer.addAccount(account);
        Loan loan = createLoan();
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRepaymentDTO repay = new LoanRepaymentDTO();
        repay.setAmount(loan.calculateEMI());
        repay.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repay);

        mockMvc.perform(post("/api/customer/loan/repay")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.amount").value(repay.getAmount().toString()))
                .andExpect(jsonPath("$.fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.loanId").value(loan.getId()))
                .andExpect(jsonPath("$.failureReason").value("Insufficient balance!"))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()))
                .andExpect(jsonPath("$.transactionType").value(TransactionType.LOAN_REPAYMENT.toString()));
    }

    @Test
    public void whenRepayLoan_Overpay_ThenTransactionFailure() throws Exception{
        Customer customer = createCustomer(COUNT + 17);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = createLoan();
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        LoanRepaymentDTO repay = new LoanRepaymentDTO();
        repay.setAmount(loan.getOutstandingAmount().add(BigDecimal.ONE));
        repay.setLoanId(loan.getId());

        String body = objectMapper.writeValueAsString(repay);

        mockMvc.perform(post("/api/customer/loan/repay")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.loanId").value(loan.getId()))
                .andExpect(jsonPath("$.failureReason").value("You are paying more than required amount."))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()))
                .andExpect(jsonPath("$.transactionType").value(TransactionType.LOAN_REPAYMENT.toString()));
    }

    @Test
    public void whenGetParticularLoan_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 18);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        mockMvc.perform(get("/api/customer/loan/{loanId}", loan.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(loan.getId()))
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.tenureInMonths").value(loan.getTenureInMonths()))
                .andExpect(jsonPath("$.loanStatus").value(loan.getLoanStatus().toString()))
                .andExpect(jsonPath("$.loanType").value(loan.getLoanType().toString()))
                .andExpect(jsonPath("$.principalAmount").value("2000.0"))
                .andExpect(jsonPath("$.rateOfInterest").value("13.0"));
    }

    @Test
    public void whenGetParticularLoan_NoLoan_ThenNotFound() throws Exception{
        Customer customer = createCustomer(COUNT + 19);
        Loan loan = new Loan();
        loan.setId(99999999999999L);
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customer/loan/{loanId}", loan.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Loan not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetParticularLoan_OtherCustomer_ThenForbidden() throws Exception{
        Customer customer1 = createCustomer(COUNT + 20);
        Customer customer = createCustomer(COUNT + 21);
        Account account = createAccount();
        Loan loan = createLoan();
        customer1.addAccount(account);
        account.addLoan(loan);
        customerRepository.saveAll(List.of(customer, customer1));
        accountRepository.save(account);
        loanRepository.save(loan);

        mockMvc.perform(get("/api/customer/loan/{loanId}", loan.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Loan access denied."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenGetLoanTransactions_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 22);
        Account account = createAccount();
        Loan loan = createLoan();
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            Transaction transaction = new Transaction();
            transaction.setDateOfTransaction(LocalDateTime.now().minusMonths(i % 8));
            transaction.setAmount(loan.calculateEMI());

            if(i % 5 == 0) transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            else if(i % 5 == 1) transaction.setTransactionStatus(TransactionStatus.CANCELLED);
            else if(i % 5 == 2) transaction.setTransactionStatus(TransactionStatus.PENDING);
            else if(i % 5 == 3) transaction.setTransactionStatus(TransactionStatus.REVERSED);
            else transaction.setTransactionStatus(TransactionStatus.FAILED);

            if(i % 10 == 0) transaction.setTransactionType(TransactionType.LOAN_DISBURSEMENT);
            else transaction.setTransactionType(TransactionType.LOAN_REPAYMENT);

            loan.addTransaction(transaction);
            transactions.add(transaction);
        }
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);
        transactionRepository.saveAll(transactions);

        mockMvc.perform(get("/api/customer/loan/{loanId}/transactions", loan.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(30));
    }

    @Test
    public void whenGetLoanTransactions_WithFilters_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 23);
        Account account = createAccount();
        Loan loan = createLoan();
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            Transaction transaction = new Transaction();
            transaction.setDateOfTransaction(LocalDateTime.now().minusMonths(i % 8));
            transaction.setAmount(loan.calculateEMI());

            if(i % 5 == 0) transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            else if(i % 5 == 1) transaction.setTransactionStatus(TransactionStatus.CANCELLED);
            else if(i % 5 == 2) transaction.setTransactionStatus(TransactionStatus.PENDING);
            else if(i % 5 == 3) transaction.setTransactionStatus(TransactionStatus.REVERSED);
            else transaction.setTransactionStatus(TransactionStatus.FAILED);

            if(i % 10 == 1) transaction.setTransactionType(TransactionType.LOAN_DISBURSEMENT);
            else transaction.setTransactionType(TransactionType.LOAN_REPAYMENT);

            loan.addTransaction(transaction);
            transactions.add(transaction);
        }
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);
        transactionRepository.saveAll(transactions);

        mockMvc.perform(get("/api/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .param("page", "1")
                        .param("size", "3")
                        .param("type", TransactionType.LOAN_REPAYMENT.toString())
                        .param("status", TransactionStatus.SUCCESS.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].transactionType").value(TransactionType.LOAN_REPAYMENT.toString()))
                .andExpect(jsonPath("$.content[0].transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(jsonPath("$.pageable.pageSize").value(3));
    }

    @Test
    public void whenGetLoanTransaction_NoLoan_ThenNotFound() throws Exception{
        Customer customer = createCustomer(COUNT + 24);
        Loan loan = new Loan();
        loan.setId(9999999999999L);
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Loan not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetLoanTransaction_OtherCustomer_ThenForbidden() throws Exception{
        Customer customer1 = createCustomer(COUNT + 25);
        Customer customer = createCustomer(COUNT + 26);
        Account account = createAccount();
        Loan loan = createLoan();
        account.addLoan(loan);
        customer1.addAccount(account);
        customerRepository.saveAll(List.of(customer1, customer));
        accountRepository.save(account);
        loanRepository.save(loan);

        mockMvc.perform(get("/api/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Loan access denied."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 27);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").exists())
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.tenureInMonths").value(requestDTO.getTenureInMonths()))
                .andExpect(jsonPath("$.loanType").value(requestDTO.getLoanType().toString()))
                .andExpect(jsonPath("$.loanStatus").value(LoanStatus.PENDING.toString()))
                .andExpect(jsonPath("$.principalAmount").value(requestDTO.getPrincipalAmount().toString()));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenCreateLoanByEmployee_WrongRole_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 28);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenCreateLoanByEmployee_NoEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 29);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = new Employee();
        employee.setUsername("IDoNotExist");

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_InactiveEmployee_ThenUnauthorized() throws Exception{
        Customer customer = createCustomer(COUNT + 30);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP + 1);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_NoAccount_ThenNotFound() throws Exception{
        Account account = new Account();
        account.setId(9999999999L);

        Employee employee = createEmployee(EMP + 2);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_InactiveAccount_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 31);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.INACTIVE);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP + 3);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not active."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_NonPositiveTenure_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 32);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP + 4);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());
        requestDTO.setTenureInMonths(-10);

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tenure in months cannot be non-positive."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_NonPositiveLoan_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(COUNT + 33);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP + 5);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());
        requestDTO.setPrincipalAmount(BigDecimal.valueOf(-10));

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Entered amount is invalid. Please enter a positive amount."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateLoanByEmployee_OverdueLoan_ThenConflict() throws Exception {
        Customer customer = createCustomer(COUNT + 34);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = createLoan();
        loan.setDateOfIssuance(LocalDate.of(2000, 12, 31));
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = createEmployee(EMP + 6);
        employeeRepository.save(employee);

        LoanRequestDTO requestDTO = createLoanRequest();
        requestDTO.setAccountId(account.getId());

        String body = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/employee/customer/account/{accountId}/loan/apply", account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You have an overdue loan! Cannot create a new loan."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void whenGetAllLoansByEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 35);
        Account account = createAccount();
        customer.addAccount(account);
        List<Loan> loans = new ArrayList<>();

        for(int i = 0; i < 30; i++){
            Loan loan = createLoan();
            if(i % 5 == 0) loan.setLoanType(LoanType.BUSINESS);
            else if(i % 5 == 1) loan.setLoanType(LoanType.VEHICLE);
            else if(i % 5 == 2) loan.setLoanType(LoanType.GOLD);
            else if(i % 5 == 3) loan.setLoanType(LoanType.EDUCATION);
            else loan.setLoanType(LoanType.HOME);

            if(i % 4 == 0) loan.setLoanStatus(LoanStatus.PENDING);
            else if(i % 4 == 1) loan.setLoanStatus(LoanStatus.APPROVED);
            else if(i % 4 == 2) loan.setLoanStatus(LoanStatus.DISBURSED);
            else loan.setLoanStatus(LoanStatus.CLOSED);

            account.addLoan(loan);
            loans.add(loan);
        }

        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.saveAll(loans);

        Employee employee = createEmployee(EMP + 7);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/{customerId}/loan", customer.getId())
                .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.content[0].loanType").exists())
                .andExpect(jsonPath("$.content[0].loanStatus").exists());
    }

    @Test
    public void whenGetAllLoansByEmployee_WithFilters_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 36);
        Account account = createAccount();
        customer.addAccount(account);
        List<Loan> loans = new ArrayList<>();

        for(int i = 0; i < 30; i++){
            Loan loan = createLoan();
            if(i % 5 == 0) loan.setLoanType(LoanType.BUSINESS);
            else if(i % 5 == 1) loan.setLoanType(LoanType.VEHICLE);
            else if(i % 5 == 2) loan.setLoanType(LoanType.GOLD);
            else if(i % 5 == 3) loan.setLoanType(LoanType.EDUCATION);
            else loan.setLoanType(LoanType.HOME);

            if(i % 4 == 0) loan.setLoanStatus(LoanStatus.PENDING);
            else if(i % 4 == 1) loan.setLoanStatus(LoanStatus.APPROVED);
            else if(i % 4 == 2) loan.setLoanStatus(LoanStatus.DISBURSED);
            else loan.setLoanStatus(LoanStatus.CLOSED);

            account.addLoan(loan);
            loans.add(loan);
        }

        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.saveAll(loans);

        Employee employee = createEmployee(EMP + 8);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/{customerId}/loan", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .param("page", "1")
                        .param("size", "3")
                        .param("status", LoanStatus.APPROVED.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].loanStatus").value(LoanStatus.APPROVED.toString()))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(jsonPath("$.pageable.pageSize").value(3));
    }

    @Test
    public void whenGetAllLoansByEmployee_WrongRole_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 37);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP + 9);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/{customerId}/loan", customer.getId())
                .with(user(employee.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenGetAllLoansByEmployee_NoEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 38);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = new Employee();
        employee.setUsername("IDoNotExist");

        mockMvc.perform(get("/api/employee/customer/{customerId}/loan", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetAllLoansByEmployee_InactiveEmployee_ThenUnauthorized() throws Exception{
        Customer customer = createCustomer(COUNT + 39);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(EMP + 10);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/{customerId}/loan", customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenGetParticularLoanByEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 40);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = createEmployee(EMP + 11);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(loan.getId()))
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.tenureInMonths").value(loan.getTenureInMonths()))
                .andExpect(jsonPath("$.loanStatus").value(loan.getLoanStatus().toString()))
                .andExpect(jsonPath("$.loanType").value(loan.getLoanType().toString()))
                .andExpect(jsonPath("$.principalAmount").value("2000.0"))
                .andExpect(jsonPath("$.rateOfInterest").value("13.0"));
    }

    @Test
    public void whenGetParticularLoanByEmployee_NoLoan_ThenNotFound() throws Exception{
        Customer customer = createCustomer(COUNT + 41);
        Loan loan = new Loan();
        loan.setId(99999999999999L);
        customerRepository.save(customer);

        Employee employee = createEmployee(EMP + 12);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Loan not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetParticularLoanByEmployee_WrongRole_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 42);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = createEmployee(EMP + 13);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}", loan.getId())
                        .with(user(employee.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenGetParticularLoanByEmployee_NoEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 43);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = new Employee();
        employee.setUsername("IDoNotExist");

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetParticularLoanByEmployee_InactiveEmployee_ThenUnauthorized() throws Exception{
        Customer customer = createCustomer(COUNT + 44);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = createEmployee(EMP + 14);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenGetLoanTransactionsByEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 45);
        Account account = createAccount();
        Loan loan = createLoan();
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            Transaction transaction = new Transaction();
            transaction.setDateOfTransaction(LocalDateTime.now().minusMonths(i % 8));
            transaction.setAmount(loan.calculateEMI());

            if(i % 5 == 0) transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            else if(i % 5 == 1) transaction.setTransactionStatus(TransactionStatus.CANCELLED);
            else if(i % 5 == 2) transaction.setTransactionStatus(TransactionStatus.PENDING);
            else if(i % 5 == 3) transaction.setTransactionStatus(TransactionStatus.REVERSED);
            else transaction.setTransactionStatus(TransactionStatus.FAILED);

            if(i % 10 == 0) transaction.setTransactionType(TransactionType.LOAN_DISBURSEMENT);
            else transaction.setTransactionType(TransactionType.LOAN_REPAYMENT);

            loan.addTransaction(transaction);
            transactions.add(transaction);
        }
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);
        transactionRepository.saveAll(transactions);

        Employee employee = createEmployee(EMP + 15);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(30));
    }

    @Test
    public void whenGetLoanTransactionsByEmployee_WithFilters_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 46);
        Account account = createAccount();
        Loan loan = createLoan();
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            Transaction transaction = new Transaction();
            transaction.setDateOfTransaction(LocalDateTime.now().minusMonths(i % 8));
            transaction.setAmount(loan.calculateEMI());

            if(i % 5 == 0) transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            else if(i % 5 == 1) transaction.setTransactionStatus(TransactionStatus.CANCELLED);
            else if(i % 5 == 2) transaction.setTransactionStatus(TransactionStatus.PENDING);
            else if(i % 5 == 3) transaction.setTransactionStatus(TransactionStatus.REVERSED);
            else transaction.setTransactionStatus(TransactionStatus.FAILED);

            if(i % 10 == 1) transaction.setTransactionType(TransactionType.LOAN_DISBURSEMENT);
            else transaction.setTransactionType(TransactionType.LOAN_REPAYMENT);

            loan.addTransaction(transaction);
            transactions.add(transaction);
        }
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);
        transactionRepository.saveAll(transactions);

        Employee employee = createEmployee(EMP + 16);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .param("page", "1")
                        .param("size", "3")
                        .param("type", TransactionType.LOAN_REPAYMENT.toString())
                        .param("status", TransactionStatus.SUCCESS.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].transactionType").value(TransactionType.LOAN_REPAYMENT.toString()))
                .andExpect(jsonPath("$.content[0].transactionStatus").value(TransactionStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(jsonPath("$.pageable.pageSize").value(3));
    }

    @Test
    public void whenGetLoanTransactionByEmployee_NoLoan_ThenNotFound() throws Exception{
        Customer customer = createCustomer(COUNT + 47);
        Loan loan = new Loan();
        loan.setId(9999999999999L);
        customerRepository.save(customer);

        Employee employee = createEmployee(EMP + 17);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Loan not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetLoanTransactionByEmployee_WrongRole_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 48);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = createEmployee(EMP + 18);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(employee.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenGetLoanTransactionByEmployee_NoEmployee_ThenOk() throws Exception{
        Customer customer = createCustomer(COUNT + 49);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = new Employee();
        employee.setUsername("IDoNotExist");

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenGetLoanTransactionByEmployee_InactiveEmployee_ThenUnauthorized() throws Exception{
        Customer customer = createCustomer(COUNT + 50);
        Account account = createAccount();
        Loan loan = createLoan();
        customer.addAccount(account);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        loanRepository.save(loan);

        Employee employee = createEmployee(EMP + 19);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/employee/customer/loan/{loanId}/transactions", loan.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }
}