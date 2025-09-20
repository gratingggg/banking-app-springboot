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
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerAccountControllerTest {
    private final MockMvc mockMvc;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmployeeRepository employeeRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomerAccountControllerTest(MockMvc mockMvc,
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

    private Customer createCustomer(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        Customer customer = new Customer();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword(passwordEncoder.encode("secret" + i));
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + num, 1, 1));
        customer.setAadharNo("123123123" + i );
        customer.setPhoneNumber("1234123" + i);
        return customer;
    }

    private Account createAccount(){
        Account account = new Account();
        account.setDateOfIssuance(LocalDate.now());
        account.setAccountType(AccountType.CURRENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return account;
    }

    private Employee createEmployee(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
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

    private Transaction createTransaction(int i){
        Transaction transaction = new Transaction();
        transaction.setLoan(null);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(i * i * 100));
        transaction.setDateOfTransaction(LocalDateTime.now());

        Employee employee = createEmployee(16);
        transaction.setHandledBy(employee);
        employeeRepository.save(employee);

        return transaction;
    }

    @Test
    public void whenViewCustomerAccounts_ThenOk() throws Exception{
        Customer customer = createCustomer(28);

        Account account0 = createAccount();
        account0.setCustomer(customer);
        Account account1 = createAccount();
        account1.setAccountType(AccountType.SAVINGS);
        account0.setCustomer(customer);

        customer.addAccount(account0);
        customer.addAccount(account1);
        customerRepository.save(customer);
        accountRepository.saveAll(List.of(account0, account1));

        mockMvc.perform(get("/api/customer/accounts")
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").exists())
                .andExpect(jsonPath("$[0].accountType").value(account0.getAccountType().toString()))
                .andExpect(jsonPath("$[1].accountId").exists())
                .andExpect(jsonPath("$[1].accountType").value(account1.getAccountType().toString()));
    }

    @Test
    public void whenViewCustomerAccounts_ThenNoAccount() throws Exception{
        Customer customer = createCustomer(29);
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customer/accounts")
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void whenViewCustomerAccountsButCustomerDoesNotExist_ThenNotFound() throws Exception{
        Customer customer = new Customer();
        customer.setUsername("IDoNotExist");

        mockMvc.perform(get("/api/customer/accounts")
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenNoCustomer_ThenUnauthorized() throws Exception{
        mockMvc.perform(get("/api/customer/accounts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/customer/accounts/123456789/transactions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/customer/accounts/123456789/balance"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/customer/accounts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/customer/accounts/close"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void whenViewParticularCustomerAccount_ThenOk() throws Exception{
        Customer customer = createCustomer(30);

        Account account = createAccount();
        account.setCustomer(customer);

        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}", account.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.accountType").value(account.getAccountType().toString()))
                .andExpect(jsonPath("$.accountStatus").value(account.getAccountStatus().toString()))
                .andExpect(jsonPath("$.dateOfIssuance").value(account.getDateOfIssuance().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.customerName").value(customer.getName()));
    }

    @Test
    public void whenViewParticularCustomerAccountButCustomerDoesNotExist_ThenNotFound() throws Exception{
        Customer customer = createCustomer(100);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}", account.getId())
                .with(user("IDONotExist").roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewParticularCustomerAccountWithInvalidAcocuntId_ThenAccountNotFound() throws Exception{
        Customer customer = createCustomer(101);
        customerRepository.save(customer);

        Long wrongAccountId = 9999999999999L;
        mockMvc.perform(get("/api/customer/accounts/{wrongAccountId}", wrongAccountId)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewParticularCustomerAccountButWithOtherCustomerAccount_ThenUnauthorized() throws Exception{
        Customer customer0 = createCustomer(102);
        Account account = createAccount();
        account.setCustomer(customer0);
        customer0.addAccount(account);

        Customer customer1 = createCustomer(103);

        customerRepository.saveAll(List.of(customer0, customer1));
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."));
    }

    @Test
    public void whenDeleteAccountWithInvalidAccount_ThenAccountNotFound() throws Exception{
        Customer customer = createCustomer(31);
        customerRepository.save(customer);

        Long wrongAccountId = 123123123123L;

        mockMvc.perform(post("/api/customer/accounts/{wrongAccountId}/close", wrongAccountId)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));

    }

    @Test
    public void whenDeleteOtherCustomerAccount_ThenAccountAccessDenied() throws Exception{
        Customer customer0 = createCustomer(32);
        Account account = createAccount();
        account.setCustomer(customer0);
        customer0.addAccount(account);

        Customer customer1 = createCustomer(33);

        customerRepository.saveAll(List.of(customer0, customer1));
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."));
    }

    @Test
    public void whenViewAllTransaction_ThenOk() throws  Exception{
        Customer customer = createCustomer(34);
        Account account = createAccount();
        account.setCustomer(customer);
        customer.addAccount(account);

        Transaction transaction0 = createTransaction(1);
        Transaction transaction1 = createTransaction(2);
        Transaction transaction2 = createTransaction(3);
        transaction2.setFromAccount(account);
        transaction1.setFromAccount(account);
        transaction0.setFromAccount(account);

        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.saveAll(List.of(transaction0, transaction1, transaction2));

        mockMvc.perform(get("/api/customer/accounts/{accountId}/transactions", account.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value(transaction0.getId()))
                .andExpect(jsonPath("$.content[0].fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.content[1].transactionId").value(transaction1.getId()))
                .andExpect(jsonPath("$.content[1].fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.content[2].transactionId").value(transaction2.getId()))
                .andExpect(jsonPath("$.content[2].fromAccountId").value(account.getId()));
    }

    @Test
    public void whenNoTransaction_ThenEmptyList() throws Exception{
        Customer customer = createCustomer(35);
        Account account = createAccount();
        account.setCustomer(customer);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}", account.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk());
    }

    @Test
    public void whenViewAllTransactionsWithInvalidCustomer_ThenNotFound() throws Exception{
        Customer customer = createCustomer(104);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/transactions", account.getId())
                .with(user("IDoNotExist").roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllTransactionsWithInvalidAccountId_ThenNotFound() throws Exception{
        Customer customer = createCustomer(105);
        customerRepository.save(customer);

        Long wrongAccountId = 123123123123L;
        mockMvc.perform(get("/api/customer/accounts/{wrongAccountId}/transactions", wrongAccountId)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."));
    }

    @Test
    public void whenViewOtherCustomerTransactions_ThenUnauthorized() throws Exception{
        Customer customer0 = createCustomer(106);
        Account account = createAccount();
        account.setCustomer(customer0);
        customer0.addAccount(account);

        Customer customer1 = createCustomer(107);

        customerRepository.saveAll(List.of(customer0, customer1));
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/transactions", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenViewBalance_ThenOk() throws Exception{
        Customer customer = createCustomer(36);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(2000));
        account.setCustomer(customer);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/balance", account.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(2000.0));
    }

    @Test
    public void whenViewBalanceButCustomerDoesNotExist_ThenNotFound() throws Exception{
        Customer customer = createCustomer(108);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/balance", account.getId())
                .with(user("IDoNotExist").roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewBalanceWithInvalidAccount_ThenNotFound() throws Exception{
        Customer customer = createCustomer(109);
        customerRepository.save(customer);

        Long wrongAccountId = 123123123123L;

        mockMvc.perform(get("/api/customer/accounts/{wrongAccountId}/balance", wrongAccountId)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewOtherCustomerBalance_ThenUnauthorized() throws Exception{
        Customer customer0 = createCustomer(110);
        Account account = createAccount();
        account.setCustomer(customer0);
        customer0.addAccount(account);

        Customer customer1 = createCustomer(111);

        customerRepository.saveAll(List.of(customer0, customer1));
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/balance", account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void whenViewBalanceWithInactiveAccount_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(112);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.INACTIVE);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/balance", account.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your account is currently not active."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenCreateAccount_ThenOk() throws Exception{
        Customer customer = createCustomer(37);
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.CURRENT);
        customerRepository.save(customer);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.customerName").value(customer.getName()))
                .andExpect(jsonPath("$.accountType").value(requestDTO.getAccountType().toString()))
                .andExpect(jsonPath("$.dateOfIssuance").value(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                .andExpect(jsonPath("$.balance").value("0"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    @Test
    public void whenCreateAccountWithInvalidCustomer_ThenNotFound() throws Exception{
        Customer customer = new Customer();
        customer.setUsername("IDoNotExist");
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.CURRENT);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenNoAccountType_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(38);
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        customerRepository.save(customer);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateAccountWithSameAccountType_ThenDuplicateAccount() throws Exception {
        Customer customer = createCustomer(39);
        Account account = createAccount();
        customer.addAccount(account);
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.CURRENT);
        customerRepository.save(customer);
        accountRepository.save(account);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The account with given type already exists."));
    }

    @Test
    public void whenDeleteAccountInactive_ThenException() throws Exception{
        Customer customer = createCustomer(40);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.INACTIVE);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your account is currently not active."));
    }

    @Test
    public void whenAccountBalanceNotZero_ThenNoDeletion() throws Exception{
        Customer customer = createCustomer(41);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(1000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account balance is not zero. Please withdraw the remaining money before deleting the account."));

    }

    @Test
    public void whenDeleteAccount_ThenOk() throws Exception{
        Customer customer = createCustomer(42);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isOk());

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(AccountStatus.CLOSED, updatedAccount.getAccountStatus());
    }

    @Test
    public void whenDeleteAccountButCustomerDoesNotExist_ThenNotFound() throws Exception{
        Customer customer = createCustomer(113);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                .with(user("IDoNotExist").roles(customer.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenActiveLoans_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(43);
        Account account = createAccount();
        Loan loan = new Loan();
        Employee employee = createEmployee(17);
        employee.addApprovedLoan(loan);
        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setLoanType(LoanType.BUSINESS);
        loan.setDateOfIssuance(LocalDate.now());
        loan.setPrincipalAmount(BigDecimal.valueOf(10000));
        loan.setRateOfInterest(BigDecimal.valueOf(13));
        loan.setTenureInMonths(36);
        loan.setAccount(account);
        account.addLoan(loan);
        customer.addAccount(account);
        customerRepository.save(customer);
        employeeRepository.save(employee);
        accountRepository.save(account);
        loanRepository.save(loan);
        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You currently have active loan. Please clear them before deleting the account."));
    }


}
