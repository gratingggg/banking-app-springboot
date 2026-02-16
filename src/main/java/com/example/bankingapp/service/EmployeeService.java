package com.example.bankingapp.service;

import com.example.bankingapp.dto.employee.EmployeeResponseDTO;
import com.example.bankingapp.dto.loan.LoanResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.exception.*;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.EmployeeRepository;
import com.example.bankingapp.repository.LoanRepository;
import com.example.bankingapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           LoanRepository loanRepository,
                           TransactionRepository transactionRepository,
                           NotificationService notificationService,
                           AccountRepository accountRepository) {
        this.employeeRepository = employeeRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
    }

    private EmployeeResponseDTO mapEmployeeToDTO(Employee employee) {
        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
        employeeResponseDTO.setAddress(employee.getAddress());
        employeeResponseDTO.setEmail(employee.getEmail());
        employeeResponseDTO.setGender(employee.getGender());
        employeeResponseDTO.setName(employee.getName());
        employeeResponseDTO.setPhoneNumber(employee.getPhoneNumber());
        employeeResponseDTO.setUsername(employee.getUsername());
        employeeResponseDTO.setDateOfBirth(employee.getDateOfBirth());
        employeeResponseDTO.setEmployeeRole(employee.getEmployeeRole());
        employeeResponseDTO.setEmployeeStatus(employee.getEmployeeStatus());

        return employeeResponseDTO;
    }

    private Employee validateEmployee(String username) {
        Employee employee = employeeRepository.findByUsername(username).orElseThrow(EmployeeNotFoundException::new);
        if (!employee.getEmployeeStatus().equals(EmployeeStatus.ACTIVE)) throw new EmployeeInactiveException();
        return employee;
    }

    private Transaction getTransaction(BigDecimal fund, Account account){
        Transaction transaction = new Transaction();
        transaction.setDateOfTransaction(LocalDateTime.now());
        transaction.setAmount(fund);
        transaction.setToAccount(account);
        return transaction;
    }

    public EmployeeResponseDTO getMyDetails(String username) {

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (employee.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeInactiveException();
        }

        return mapEmployeeToDTO(employee);
    }

    public LoanResponseDTO processLoan(Long loanId, String action, String username){
        Employee employee = validateEmployee(username);

        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);

        if (loan.getLoanStatus() != LoanStatus.PENDING)
            throw new LoanAlreadyProcessedException();

        if (action.equalsIgnoreCase("APPROVE")) {
            loan.setLoanStatus(LoanStatus.APPROVED);
        } else if (action.equalsIgnoreCase("REJECT")) {
            loan.setLoanStatus(LoanStatus.REJECTED);
        } else {
            throw new InvalidActionException("Action must be APPROVE or REJECT or DISBURSE");
        }

        loan.setApprovedBy(employee);

        loanRepository.save(loan);

        return new LoanResponseDTO(loan);
    }

    @Transactional
    public TransactionResponseDTO disburseLoan(Long loanId, String username){
        Employee employee = validateEmployee(username);

        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);

        if(loan.getLoanStatus() == LoanStatus.DISBURSED)
            throw new LoanAlreadyProcessedException("Loan is already disbursed.");
        if (loan.getLoanStatus() != LoanStatus.APPROVED)
            throw new LoanAlreadyProcessedException("Loan is not approved yet.");

        Account account = loan.getAccount();
        if(account.getAccountStatus() != AccountStatus.ACTIVE)
            throw new AccountNotActiveException("Account is not active.");

        Transaction transaction = getTransaction(loan.getPrincipalAmount(), account);
        transaction.setTransactionType(TransactionType.LOAN_DISBURSEMENT);
        employee.addHandledTransaction(transaction);

        account.deposit(loan.getPrincipalAmount());
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setFailureReason(null);
        transaction.setLoan(loan);

        String message = "Dear " + account.getCustomer().getName() +
                ", we are pleased to inform you that your loan (ID: " + loan.getId() +
                ") for " + loan.getPrincipalAmount() +
                " has been credited to your account (A/C " + account.getId() +
                ") on " + transaction.getDateOfTransaction() + ".";

        loan.setLoanStatus(LoanStatus.DISBURSED);

        transactionRepository.save(transaction);
        accountRepository.save(account);
        employeeRepository.save(employee);

        notificationService.createNotification(account.getCustomer(), NotificationType.TRANSACTION, message);

        loan.setDateOfIssuance(LocalDate.now());
        loanRepository.save(loan);

        return new TransactionResponseDTO(transaction);
    }

}
