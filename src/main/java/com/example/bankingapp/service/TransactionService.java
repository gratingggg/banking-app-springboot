package com.example.bankingapp.service;

import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionSummaryDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.exception.*;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.CustomerRepository;
import com.example.bankingapp.repository.EmployeeRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.specification.TransactionSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              CustomerRepository customerRepository,
                              AccountRepository accountRepository,
                              EmployeeRepository employeeRepository,
                              NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    private void validateAccountAndAmount(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Your account is currently " + fromAccount.getAccountStatus() + ". Please contact the admin.");
        }
        if (toAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("The recipient's account is currently " + toAccount.getAccountStatus() + ".");
        }
        if(fromAccount.equals(toAccount)){
            throw new SameAccountTransactionException();
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountInvalidException();
        }
    }

    private Transaction getTransaction(BigDecimal fund, Account fromAccount, Account toAccount) {
        Transaction transaction = new Transaction();
        transaction.setDateOfTransaction(LocalDateTime.now());
        transaction.setAmount(fund);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        return transaction;
    }

    private Employee validateEmployeeFromUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username).orElseThrow(() -> new EmployeeNotFoundException("Employee with username " + username + " not found."));
        if (employee.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeInactiveException();
        }

        return employee;
    }

    private Page<TransactionSummaryDTO> getAllTransactions(Customer customer, int page, int size, TransactionStatus status,
                                                            TransactionType type, LocalDate fromDate, LocalDate toDate){
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction").descending());
        Specification<Transaction> specification = TransactionSpecifications.forCustomers(customer.getId())
                .and(TransactionSpecifications.withStatus(status))
                .and(TransactionSpecifications.withType(type))
                .and(TransactionSpecifications.dateBetween(fromDate, toDate));
        Page<Transaction> transactions = transactionRepository.findAll(specification, pageable);
        return transactions.map(
                transaction -> new TransactionSummaryDTO(transaction, customer)
        );
    }

    private Transaction transfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        validateAccountAndAmount(fromAccount, toAccount, amount);
        Transaction transaction = getTransaction(amount, fromAccount, toAccount);
        transaction.setTransactionType(TransactionType.TRANSFERRED);

        BigDecimal totalTodayAmount = amount;
        Specification<Transaction> specification = TransactionSpecifications.forFromAccounts(fromAccount)
                .and(TransactionSpecifications.dateBetween(LocalDate.now(), null));
        List<Transaction> transactionList = transactionRepository.findAll(specification);
        for (Transaction t : transactionList) {
            if (t.isDebit() || t.getTransactionType().equals(TransactionType.TRANSFERRED)) {
                totalTodayAmount = totalTodayAmount.add(t.getAmount());
            }
        }

        String fromMessage = "";
        String toMessage = "";
        if(amount.compareTo(fromAccount.getBalance()) > 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Insufficient balance.");
        }
        else if (totalTodayAmount.compareTo(BigDecimal.valueOf(50000)) > 0) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Daily payment limit reached.");
        } else {
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            fromAccount.withdrawal(amount);
            toAccount.deposit(amount);

            fromMessage = "Dear " + fromAccount.getCustomer().getName() +
                    ", " + amount + " has been debited from your account A/C " + fromAccount.getId() +
                    " on " + transaction.getDateOfTransaction() + " towards " +
                    toAccount.getCustomer().getName() + ".";
            toMessage = "Dear " + toAccount.getCustomer().getName() +
                    ", " + amount + " has been credited to your account A/C " + toAccount.getId() +
                    " on " + transaction.getDateOfTransaction() + " from " +
                    fromAccount.getCustomer().getName() + ".";
        }

        accountRepository.saveAll(List.of(fromAccount, toAccount));
        transactionRepository.save(transaction);

        if(!fromMessage.isBlank() && !toMessage.isBlank()){
            notificationService.createNotification(fromAccount.getCustomer(), NotificationType.TRANSACTION, fromMessage);
            notificationService.createNotification(toAccount.getCustomer(), NotificationType.TRANSACTION, toMessage);
        }

        return transaction;
    }

    @Transactional
    public TransactionResponseDTO transferFund(Long fromAccountId, Long toAccountId, BigDecimal amount, String username) {
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new CustomerNotFoundException("The customer with username " + username + " not found."));
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow(() -> new AccountNotFoundException("The account with id " + fromAccountId + " does not exist"));
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow(() -> new AccountNotFoundException("The account with id " + toAccountId + " does not exist"));
        if (!fromAccount.getCustomer().equals(customer)) {
            throw new TransactionAccessDeniedException();
        }
        Transaction transaction = transfer(fromAccount, toAccount, amount);

        return new TransactionResponseDTO(transaction, customer);
    }

    public TransactionResponseDTO getTransaction(Long transactionId, String username) {
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new CustomerNotFoundException("The customer with username " + username + " not found."));
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(TransactionNotFoundException::new);
        if (!((transaction.getFromAccount() != null && transaction.getFromAccount().getCustomer().equals(customer)) || (transaction.getToAccount() != null && transaction.getToAccount().getCustomer().equals(customer)))) {
            throw new TransactionAccessDeniedException();
        }

        return new TransactionResponseDTO(transaction, customer);
    }

    public TransactionResponseDTO getTransactionByEmployee(Long transactionId, String username) {
        validateEmployeeFromUsername(username);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(TransactionNotFoundException::new);
        return new TransactionResponseDTO(transaction, null);
    }

    @Transactional
    public TransactionResponseDTO transferFundByEmployee(Long fromAccountId, Long toAccountId, BigDecimal amount, String username) {
        Employee employee = validateEmployeeFromUsername(username);
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow(() -> new AccountNotFoundException("The account with id " + fromAccountId + " does not exist"));
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow(() -> new AccountNotFoundException("The account with id " + toAccountId + " does not exist"));
        Transaction transaction = transfer(fromAccount, toAccount, amount);
        transaction.setHandledBy(employee);
        employeeRepository.save(employee);
        transactionRepository.save(transaction);

        return new TransactionResponseDTO(transaction, null);
    }

    public Page<TransactionSummaryDTO> getAllTransactionsOfCustomer(Long customerId, int page, int size, TransactionStatus status,
                                                                     TransactionType type, LocalDate fromDate, LocalDate toDate, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        return getAllTransactions(customer, page, size, status, type, fromDate, toDate);
    }

    public Page<TransactionSummaryDTO> getAllTransactionsByCustomer(int page, int size, TransactionStatus status,
                                                                     TransactionType type, LocalDate fromDate, LocalDate toDate, String username) {
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new CustomerNotFoundException("The customer with username " + username + " not found."));
        return getAllTransactions(customer, page, size, status, type, fromDate, toDate);
    }
}
