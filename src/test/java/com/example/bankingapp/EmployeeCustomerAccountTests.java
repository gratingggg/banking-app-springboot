package com.example.bankingapp;

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
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.repository.*;
import com.example.bankingapp.specification.NotificationSpecifications;
import com.example.bankingapp.utils.Endpoints;
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
import java.time.LocalTime;
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
public class EmployeeCustomerAccountTests {
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
    public EmployeeCustomerAccountTests(MockMvc mockMvc,
                                        ObjectMapper objectMapper,
                                        LoanRepository loanRepository,
                                        PasswordEncoder passwordEncoder,
                                        AccountRepository accountRepository,
                                        CustomerRepository customerRepository,
                                        EmployeeRepository employeeRepository,
                                        TransactionRepository transactionRepository,
                                        NotificationRepository notificationRepository) {
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
        return account;
    }

    private Employee createEmployee(int num) {
        String i = "0" + num;
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

    @Test
    public void whenViewAllAccountsOfCustomer_ThenOk() throws Exception {
        Customer customer = createCustomer(44);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(18);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNTS_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(account.getId()))
                .andExpect(jsonPath("$[0].accountType").value(account.getAccountType().toString()));
    }

    @Test
    public void whenViewAllAccountsWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(45);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Customer customer1 = createCustomer(46);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNTS_ALL, customer.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenViewAllAccountsWithNoEmployeeExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(47);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNTS_ALL, customer.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllAccountsWithEmployeeNotActive_ThenException() throws Exception {
        Customer customer = createCustomer(48);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(19);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNTS_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenViewAllAccountsWithCustomerDoesNotExist_ThenCustomerNotFound() throws Exception {
        Customer customer = new Customer();
        customer.setId(999999999L);
        Employee employee = createEmployee(20);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNTS_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllAccountsOfCustomerWithNoAccount_ThenOk() throws Exception {
        Customer customer = createCustomer(49);
        customerRepository.save(customer);

        Employee employee = createEmployee(22);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNTS_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk());
    }

    @Test
    public void whenViewParticularAccount_ThenOk() throws Exception {
        Customer customer = createCustomer(50);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(23);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_PARTICULAR, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.accountType").value(account.getAccountType().toString()))
                .andExpect(jsonPath("$.accountStatus").value(account.getAccountStatus().toString()))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.customerName").value(account.getCustomer().getName()))
                .andExpect(jsonPath("$.dateOfIssuance").value(account.getDateOfIssuance().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
    }

    @Test
    public void whenViewParticularAccountWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(51);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Customer customer1 = createCustomer(52);
        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_PARTICULAR, account.getId())
                        .with(user(customer1.getUsername()).roles(customer1.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenViewParticularAccountWithEmployeeDoesNotExist_ThenEmployeeNotFound() throws Exception {
        Customer customer = createCustomer(52);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_PARTICULAR, account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewParticularAccountWithoutActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(53);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(24);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_PARTICULAR, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenViewParticularAccountWithInvalidAccountId_ThenNotFound() throws Exception {
        Account account = new Account();
        account.setId(999999999L);

        Employee employee = createEmployee(25);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_PARTICULAR, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateAccount_ThenOk() throws Exception {
        Customer customer = createCustomer(54);
        customerRepository.save(customer);

        Employee employee = createEmployee(26);
        employeeRepository.save(employee);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_CREATE, customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.customerName").value(customer.getName()));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenCreateAccountWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(55);
        customerRepository.save(customer);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_CREATE, customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user("IAmWrongRole").roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenCreateAccountButEmployeeDoesNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(56);
        customerRepository.save(customer);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_CREATE, customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateAccountWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(57);
        customerRepository.save(customer);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(27);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_CREATE, customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));

    }

    @Test
    public void whenCreateAccountWithCustomerNotExist_ThenNotFound() throws Exception {
        Customer customer = new Customer();
        customer.setId(999999999L);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.SAVINGS);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(28);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_CREATE, customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenCreateAccountWithDuplicateAccountType_ThenException() throws Exception {
        Customer customer = createCustomer(58);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.CURRENT);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        Employee employee = createEmployee(29);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_CREATE, customer.getId())
                        .contentType("application/json")
                        .content(requestBody)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The account with given type already exists."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void whenDeleteAccount_ThenOk() throws Exception {
        Customer customer = createCustomer(59);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(30);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.accountType").value(account.getAccountType().toString()))
                .andExpect(jsonPath("$.accountStatus").value("CLOSED"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.customerName").value(account.getCustomer().getName()))
                .andExpect(jsonPath("$.dateOfIssuance").value(account.getDateOfIssuance().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenDeleteAccountWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(60);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(31);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(customer.getUsername()).roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenDeleteAccountWithEmployeeDoesNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(61);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenDeleteAccountWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(62);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(32);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenDeleteAccountWithAccountDoesNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(63);
        customerRepository.save(customer);

        Account account = new Account();
        account.setId(999999999L);

        Employee employee = createEmployee(33);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenDeleteAccountWithNotActiveAccount_ThenException() throws Exception {
        Customer customer = createCustomer(64);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.CLOSED);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(34);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not active."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenDeleteAccountWithActiveLoan_ThenException() throws Exception {
        Customer customer = createCustomer(65);
        Account account = createAccount();
        customer.addAccount(account);
        Loan loan = new Loan();
        Employee employee = createEmployee(35);
        employee.addApprovedLoan(loan);
        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setLoanType(LoanType.BUSINESS);
        loan.setDateOfIssuance(LocalDate.now());
        loan.setPrincipalAmount(BigDecimal.valueOf(10000));
        loan.setRateOfInterest(BigDecimal.valueOf(13));
        loan.setTenureInMonths(36);
        account.addLoan(loan);
        customerRepository.save(customer);
        accountRepository.save(account);
        employeeRepository.save(employee);
        loanRepository.save(loan);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You currently have active loan. Please clear them before deleting the account."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenDeleteAccountWithNonZeroBalance_ThenException() throws Exception {
        Customer customer = createCustomer(66);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(2000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(36);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DELETE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account balance is not zero. Please withdraw the remaining money before deleting the account."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenViewAllTransactionsOfAccount_ThenOk() throws Exception {
        Customer customer = createCustomer(67);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setToAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        // After saving, verify the data
        System.out.println("Account ID: " + account.getId());
        System.out.println("Transaction ID: " + transaction.getId());
        System.out.println("Transaction fromAccount ID: " +
                (transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : "null"));
        System.out.println("Transaction toAccount ID: " +
                (transaction.getToAccount() != null ? transaction.getToAccount().getId() : "null"));

        Employee employee = createEmployee(37);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value(transaction.getId()))
                .andExpect(jsonPath("$.content[0].dateOfTransaction").value(transaction.getDateOfTransaction().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))))
                .andExpect(jsonPath("$.content[0].amount").value("299.0"))
                .andExpect(jsonPath("$.content[0].toAccountId").value(account.getId()))
                .andExpect(jsonPath("$.content[0].transactionType").value(transaction.getTransactionType().toString()))
                .andExpect(jsonPath("$.content[0].transactionStatus").value(transaction.getTransactionStatus().toString()))
                .andExpect(jsonPath("$.content[0].fromAccountId").doesNotExist())
                .andExpect(jsonPath("$.content[0].loanId").doesNotExist())
                .andExpect(jsonPath("$.content[0].failureReason").doesNotExist());
    }

    @Test
    public void whenViewAllTransactionsOfAccountWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(68);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .with(user("IAmWrongRole").roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenViewAllTransactionsOfAccountWithEmployeeNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(69);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllTransactionsOfAccountWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(70);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        Employee employee = createEmployee(61);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenViewAllTransactionsOfAccountButAccountDoesNotExist_ThenNotFound() throws Exception {
        Account account = new Account();
        account.setId(999999999L);
        Employee employee = createEmployee(38);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllTransactionsOfAccountWithNoTransactions_ThenOk() throws Exception {
        Customer customer = createCustomer(71);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(39);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk());
    }

    @Test
    public void whenViewAllTransactionsOfAccountWithPageRequests_ThenOk() throws Exception {
        Customer customer = createCustomer(72);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        for (int i = 0; i < 10; i++) {
            Transaction transaction = createTransaction();
            transaction.setFromAccount(account);
            transaction.setAmount(BigDecimal.valueOf(i * i * i * 10));
            transactionRepository.save(transaction);
        }

        Employee employee = createEmployee(40);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL, account.getId())
                        .param("page", "2")
                        .param("size", "3")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").exists())
                .andExpect(jsonPath("$.content[0].amount").value(2160.0))
                .andExpect(jsonPath("$.content[1].transactionId").exists())
                .andExpect(jsonPath("$.content[1].amount").value(3430.0))
                .andExpect(jsonPath("$.content[2].transactionId").exists())
                .andExpect(jsonPath("$.content[2].amount").value(5120.0));
    }

    @Test
    public void whenViewAllTransactions_ThenOk() throws Exception {
        Customer customer = createCustomer(73);
        Account account0 = createAccount();
        Account account1 = createAccount();
        Transaction transaction0 = createTransaction();
        Transaction transaction1 = createTransaction();
        transaction0.setFromAccount(account0);
        transaction1.setFromAccount(account1);
        customer.addAccount(account0);
        customer.addAccount(account1);
        customerRepository.save(customer);
        accountRepository.saveAll(List.of(account0, account1));
        transactionRepository.saveAll(List.of(transaction0, transaction1));

        Employee employee = createEmployee(41);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value(transaction0.getId()))
                .andExpect(jsonPath("$.content[1].transactionId").value(transaction1.getId()));
    }

    @Test
    public void whenViewAllTransactionsWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(74);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .with(user("IAmWrongRole").roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenViewAllTransactionsWithEmployeeNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(75);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllTransactionWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(76);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        customer.addAccount(account);
        transaction.setFromAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        Employee employee = createEmployee(42);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenViewAllTransactionsWithCustomerNotExist_ThenNotFound() throws Exception {
        Customer customer = new Customer();
        customer.setId(999999L);
        Employee employee = createEmployee(43);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenViewAllTransactionsWithPageRequests_ThenOk() throws Exception {
        Customer customer = createCustomer(77);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        LocalDateTime baseDate = LocalDateTime.of(
                LocalDate.of(2020, 8, 15),
                LocalTime.of(7, 35, 26)
        );
        for (int i = 0; i < 10; i++) {
            Transaction transaction = createTransaction();
            transaction.setToAccount(account);
            transaction.setAmount(BigDecimal.valueOf(i * i * i * 10));
            transaction.setDateOfTransaction(baseDate.plusMonths(i));
            transactionRepository.save(transaction);
        }

        Employee employee = createEmployee(44);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .param("page", "2")
                        .param("size", "3")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").exists())
                .andExpect(jsonPath("$.content[0].amount").value(270.0))
                .andExpect(jsonPath("$.content[1].transactionId").exists())
                .andExpect(jsonPath("$.content[1].amount").value(80.0))
                .andExpect(jsonPath("$.content[2].transactionId").exists())
                .andExpect(jsonPath("$.content[2].amount").value(10.0));
    }

    @Test
    public void whenViewAllTransactionsWithNoTransactions_ThenOk() throws Exception {
        Customer customer = createCustomer(78);
        customerRepository.save(customer);

        Employee employee = createEmployee(45);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL, customer.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk());
    }

    @Test
    public void whenDepositMoney_ThenOk() throws Exception {
        Customer customer = createCustomer(79);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(46);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.toAccountId").value(account.getId()))
                .andExpect(jsonPath("$.fromAccountId").doesNotExist())
                .andExpect(jsonPath("$.dateOfTransaction").value(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))))
                .andExpect(jsonPath("$.transactionType").value(TransactionType.DEPOSIT.toString()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.SUCCESS.toString()));

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(0, updatedAccount.getBalance().compareTo(BigDecimal.valueOf(2000)));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenDepositMoneyWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(80);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user("IAmWrongRole").roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenDepositMoneyWithEmployeeNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(81);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenDepositMoneyWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(82);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(47);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenDepositMoneyWithAccountDoesNotExist_ThenNotFound() throws Exception {
        Account account = new Account();
        account.setId(9999999L);
        Employee employee = createEmployee(48);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenDepositMoneyWithMissingParams_ThenBadRequest() throws Exception {
        Customer customer = createCustomer(83);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(49);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenDepositMoneyWithNotActiveAccount_ThenBadRequest() throws Exception {
        Customer customer = createCustomer(84);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.CLOSED);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(50);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not active."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenDepositMoneyWithNegativeAmount_ThenBadRequest() throws Exception {
        Customer customer = createCustomer(85);
        Account account = createAccount();
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(51);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "-2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Entered amount is invalid. Please enter a positive amount."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenDepositMoneyButLimitReached_ThenOk() throws Exception {
        Customer customer = createCustomer(86);
        Account account = createAccount();
        Transaction transaction = createTransaction();
        transaction.setAmount(BigDecimal.valueOf(50000));
        transaction.setToAccount(account);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        Employee employee = createEmployee(52);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.transactionType").value(TransactionType.DEPOSIT.toString()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()))
                .andExpect(jsonPath("$.failureReason").value("Daily maximum deposit limit exceeded."));
    }

    @Test
    public void whenWithdrawMoney_ThenOk() throws Exception {
        Customer customer = createCustomer(87);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(53);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.toAccountId").doesNotExist())
                .andExpect(jsonPath("$.dateOfTransaction").value(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))))
                .andExpect(jsonPath("$.transactionType").value(TransactionType.WITHDRAWAL.toString()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.SUCCESS.toString()));

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(0, updatedAccount.getBalance().compareTo(BigDecimal.valueOf(3000)));

        Specification<Notification> specs = NotificationSpecifications.forCustomer(customer);
        assertEquals(1, notificationRepository.findAll(specs).size());
    }

    @Test
    public void whenWithdrawMoneyWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(88);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user("IAmWrongRole").roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenWithdrawMoneyWithEmployeeNotExist_ThenNotFound() throws Exception {
        Customer customer = createCustomer(89);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenWithdrawMoneyWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(90);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(54);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void whenWithdrawMoneyWithAccountDoesNotExist_ThenNotFound() throws Exception {
        Account account = new Account();
        account.setId(9999999L);
        Employee employee = createEmployee(55);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenWithdrawalMoneyWithMissingParams_ThenBadRequest() throws Exception {
        Customer customer = createCustomer(91);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(56);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenWithdrawMoneyWithNotActiveAccount_ThenBadRequest() throws Exception {
        Customer customer = createCustomer(92);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        account.setAccountStatus(AccountStatus.CLOSED);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(57);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not active."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenWithdrawMoneyWithNegativeAmount_ThenBadRequest() throws Exception {
        Customer customer = createCustomer(93);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(58);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "-2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Entered amount is invalid. Please enter a positive amount."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenWithdrawMoneyButLimitReached_ThenFailure() throws Exception {
        Customer customer = createCustomer(94);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(500000));
        Transaction transaction = createTransaction();
        transaction.setAmount(BigDecimal.valueOf(50000));
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setFromAccount(account);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        Employee employee = createEmployee(59);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "2000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.transactionType").value(TransactionType.WITHDRAWAL.toString()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()))
                .andExpect(jsonPath("$.failureReason").value("Daily maximum withdraw limit exceeded."));
    }

    @Test
    public void whenWithdrawMoneyWithInvalidBalance_ThenFailure() throws Exception {
        Customer customer = createCustomer(95);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(60);
        employeeRepository.save(employee);

        mockMvc.perform(post(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL, account.getId())
                        .param("fund", "7000")
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.failureReason").value("Insufficient balance."))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.FAILED.toString()));
    }

    @Test
    public void whenViewBalance_ThenOk() throws Exception {
        Customer customer = createCustomer(96);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(62);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_BALANCE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.0));
    }

    @Test
    public void whenViewBalanceWithWrongRole_ThenForbidden() throws Exception {
        Customer customer = createCustomer(97);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_BALANCE, account.getId())
                        .with(user("IAmWrongRole").roles(customer.getRole().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenViewBalanceWithInvalidEmployee_ThenNotFound() throws Exception {
        Customer customer = createCustomer(98);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_BALANCE, account.getId())
                        .with(user("IDoNotExist").roles("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void whenBalanceWithNotActiveEmployee_ThenUnauthorized() throws Exception {
        Customer customer = createCustomer(99);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(5000));
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        Employee employee = createEmployee(63);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_BALANCE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }


    @Test
    public void whenViewBalanceWithAccountDoesNotExist_ThenAccountNotFound() throws Exception {
        Account account = new Account();
        account.setId(999999999L);

        Employee employee = createEmployee(64);
        employeeRepository.save(employee);

        mockMvc.perform(get(Endpoints.EMPLOYEE_ACCOUNT_BALANCE, account.getId())
                        .with(user(employee.getUsername()).roles(employee.getRole().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }
}
