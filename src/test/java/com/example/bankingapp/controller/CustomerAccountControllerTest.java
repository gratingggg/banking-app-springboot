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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private Customer createCustomer(int i){
        Customer customer = new Customer();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword(passwordEncoder.encode("secret" + i));
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + i, 1, 1));
        customer.setAadharNo("1231231231" + i );
        customer.setPhoneNumber("12341234" + i);
        return customer;
    }

    private Account createAccount(){
        Account account = new Account();
        account.setDateOfIssuance(LocalDate.now());
        account.setAccountType(AccountType.CURRENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return account;
    }

    private Employee createEmployee(int i){
        Employee employee = new Employee();
        employee.setName("Rudra " + i + " Ceaser");
        employee.setUsername("rudra1" + i + "23");
        employee.setPassword(passwordEncoder.encode("secret" + i));
        employee.setEmail("ru" + i + "dra@example.com");
        employee.setGender(PersonGender.MALE);
        employee.setAddress("Mars" + i);
        employee.setDateOfBirth(LocalDate.of(2000 + i, 1, 1));
        employee.setPhoneNumber("12341234" + i);
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
        transaction.setDateOfTransaction(LocalDate.now());

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
                .with(user("rudra12823").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").exists())
                .andExpect(jsonPath("$[0].accountType").value("CURRENT"))
                .andExpect(jsonPath("$[1].accountId").exists())
                .andExpect(jsonPath("$[1].accountType").value("SAVINGS"));
    }

    @Test
    public void whenViewCustomerAccounts_ThenNoAccount() throws Exception{
        Customer customer = createCustomer(29);
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customer/accounts")
                .with(user("rudra12923").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
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
                .with(user("rudra13023").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.accountType").value("CURRENT"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.dateOfIssuance").value(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                .andExpect(jsonPath("$.balance").value("0.0"))
                .andExpect(jsonPath("$.customerName").value(customer.getName()));
    }

    @Test
    public void whenViewInvalidAccount_ThenAccountNotFound() throws Exception{
        Customer customer = createCustomer(31);
        customerRepository.save(customer);

        Long wrongAccountId = 123123123123L;
        mockMvc.perform(get("/api/customer/accounts/{wrongAccountId}", wrongAccountId)
                .with(user("rudra13123").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with account id "+ wrongAccountId + " does not exist."));

        mockMvc.perform(get("/api/customer/accounts/{wrongAccountId}/transactions", wrongAccountId)
                        .with(user("rudra13123").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with account id "+ wrongAccountId + " does not exist."));

        mockMvc.perform(get("/api/customer/accounts/{wrongAccountId}/balance", wrongAccountId)
                        .with(user("rudra13123").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with account id "+ wrongAccountId + " does not exist."));

        mockMvc.perform(post("/api/customer/accounts/{wrongAccountId}/close", wrongAccountId)
                        .with(user("rudra13123").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The account with account id "+ wrongAccountId + " does not exist."));

    }

    @Test
    public void whenViewOtherCustomerAccount_ThenAccountAccessDenied() throws Exception{
        Customer customer0 = createCustomer(32);
        Account account = createAccount();
        account.setCustomer(customer0);
        customer0.addAccount(account);

        Customer customer1 = createCustomer(33);

        customerRepository.saveAll(List.of(customer0, customer1));
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}", account.getId())
                .with(user("rudra13323").roles("CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."));

        mockMvc.perform(get("/api/customer/accounts/{accountId}/transactions", account.getId())
                        .with(user("rudra13323").roles("CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."));

        mockMvc.perform(get("/api/customer/accounts/{accountId}/balance", account.getId())
                        .with(user("rudra13323").roles("CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this account."));

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user("rudra13323").roles("CUSTOMER")))
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
        account.addTransaction(transaction0);
        account.addTransaction(transaction1);
        account.addTransaction(transaction2);

        customerRepository.save(customer);
        accountRepository.save(account);
        transactionRepository.saveAll(List.of(transaction0, transaction1, transaction2));

        mockMvc.perform(get("/api/customer/accounts/{accountId}/transactions", account.getId())
                .with(user("rudra13423").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value(transaction0.getId()))
                .andExpect(jsonPath("$.content[0].accountId").value(account.getId()))
                .andExpect(jsonPath("$.content[1].transactionId").value(transaction1.getId()))
                .andExpect(jsonPath("$.content[1].accountId").value(account.getId()))
                .andExpect(jsonPath("$.content[2].transactionId").value(transaction2.getId()))
                .andExpect(jsonPath("$.content[2].accountId").value(account.getId()));
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
                .with(user("rudra13523").roles("CUSTOMER")))
                .andExpect(status().isOk());
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
                .with(user("rudra13623").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("2000.0"));
    }

    @Test
    public void whenCreateAccount_ThenOk() throws Exception{
        Customer customer = createCustomer(37);
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.CURRENT);
        customerRepository.save(customer);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                        .with(user("rudra13723").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.customerName").value(customer.getName()))
                .andExpect(jsonPath("$.accountType").value("CURRENT"))
                .andExpect(jsonPath("$.dateOfIssuance").value(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                .andExpect(jsonPath("$.balance").value("0"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    @Test
    public void whenNoAccountType_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(38);
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        customerRepository.save(customer);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                        .with(user("rudra13823").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateAccountWithSameAccountType_ThenDuplicateAccount() throws Exception {
        Customer customer = createCustomer(39);
        Account account = createAccount();
        account.setCustomer(customer);
        customer.addAccount(account);
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setAccountType(AccountType.CURRENT);
        customerRepository.save(customer);
        accountRepository.save(account);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/customer/accounts")
                        .with(user("rudra13923").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The account with given type already exists."));
    }

    @Test
    public void whenAccountInactive_ThenException() throws Exception{
        Customer customer = createCustomer(40);
        Account account = createAccount();
        account.setAccountStatus(AccountStatus.INACTIVE);
        account.setCustomer(customer);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(get("/api/customer/accounts/{accountId}/balance", account.getId())
                .with(user("rudra14023").roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your account is currently not active."));

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user("rudra14023").roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your account is currently not active."));
    }

    @Test
    public void whenAccountBalanceNotZero_ThenNoDeletion() throws Exception{
        Customer customer = createCustomer(41);
        Account account = createAccount();
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCustomer(customer);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user("rudra14123").roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account balance is not zero. Please withdraw the remaining money before deleting the account."));

    }

    @Test
    public void whenDeleteAccount_ThenOk() throws Exception{
        Customer customer = createCustomer(42);
        Account account = createAccount();
        account.setCustomer(customer);
        customer.addAccount(account);
        customerRepository.save(customer);
        accountRepository.save(account);

        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                        .with(user("rudra14223").roles("CUSTOMER")))
                .andExpect(status().isOk());

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(AccountStatus.CLOSED, updatedAccount.getAccountStatus());
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
        account.setCustomer(customer);
        customer.addAccount(account);
        customerRepository.save(customer);
        employeeRepository.save(employee);
        accountRepository.save(account);
        loanRepository.save(loan);
        mockMvc.perform(post("/api/customer/accounts/{accountId}/close", account.getId())
                .with(user("rudra14323").roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You currently have active loan. Please clear them before deleting the account."));
    }
}
