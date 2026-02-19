package com.example.bankingapp.service;

import com.example.bankingapp.dto.loan.LoanRepaymentDTO;
import com.example.bankingapp.dto.loan.LoanRequestDTO;
import com.example.bankingapp.dto.loan.LoanResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import com.example.bankingapp.entities.notification.NotificationType;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.exception.*;
import com.example.bankingapp.repository.*;
import com.example.bankingapp.specification.LoanSpecification;
import com.example.bankingapp.specification.TransactionSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepostory;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository,
                       AccountRepository accountRepository,
                       CustomerRepository customerRepository,
                       EmployeeRepository employeeRepository,
                       NotificationService notificationService,
                       TransactionRepository transactionRepository){
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
        this.transactionRepostory = transactionRepository;
    }

    private void validateAccountAndLoan(Account account, LoanRequestDTO requestDTO){
        if(!account.getAccountStatus().equals(AccountStatus.ACTIVE)) throw new AccountNotActiveException();
        if(requestDTO.getTenureInMonths() <= 0) throw new IllegalArgumentException("Tenure in months cannot be non-positive.");
        if(requestDTO.getPrincipalAmount().compareTo(BigDecimal.ZERO) < 1) throw new AmountInvalidException();
    }

    private Loan loanDTOToLoan(LoanRequestDTO requestDTO){
        Loan loan = new Loan();
        loan.setLoanType(requestDTO.getLoanType());
        loan.setTenureInMonths(requestDTO.getTenureInMonths());
        loan.setPrincipalAmount(requestDTO.getPrincipalAmount());
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setRateOfInterest(getRateOfInterest(requestDTO.getLoanType()));

        return loan;
    }

    private Loan createLoan(LoanRequestDTO requestDTO, Account account){
        validateAccountAndLoan(account, requestDTO);
        Loan loan = loanDTOToLoan(requestDTO);
        account.addLoan(loan);

        for(Loan l : account.getLoans()){
            if(l.isOverdue()) {
                throw new LoanOverdueException("You have an overdue loan! Cannot create a new loan.");
            }
        }

        accountRepository.save(account);
        loanRepository.save(loan);

        return loan;
    }

    private Page<LoanResponseDTO> getAllLoans(int page, int size, LoanStatus status, LoanType type,
                                              LocalDate from, LocalDate to, Long customerId){
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfIssuance"));
        Specification<Loan> specification = LoanSpecification.forCustomer(customerId)
                .and(LoanSpecification.withStatus(status))
                .and(LoanSpecification.withType(type))
                .and(LoanSpecification.dateBetween(from, to));
        Page<Loan> pageDTO = loanRepository.findAll(specification, pageable);
        return pageDTO.map(LoanResponseDTO::new);
    }

    private TransactionResponseDTO repayLoan(LoanRepaymentDTO repaymentDTO, Customer customer){
        Loan loan = loanRepository.findByIdWithTransactions(repaymentDTO.getLoanId())
                .orElseThrow(LoanNotFoundException::new);
        if(!loan.getAccount().getCustomer().getId().equals(customer.getId())) throw new LoanAccessDeniedException();
        if(!loan.getLoanStatus().equals(LoanStatus.DISBURSED)) throw new LoanNotDisbursedException();
        if(repaymentDTO.getAmount() == null || repaymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new AmountInvalidException("Repayment amount cannot be negative.");

        Account account = accountRepository.findById(loan.getAccount().getId()).orElseThrow();

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.LOAN_REPAYMENT);
        transaction.setAmount(repaymentDTO.getAmount());
        transaction.setFromAccount(account);
        transaction.setDateOfTransaction(LocalDateTime.now());
        transaction.setLoan(loan);

        BigDecimal repayAmount = repaymentDTO.getAmount();
        BigDecimal balance = account.getBalance();
        BigDecimal outstandingAmount = loan.getOutstandingAmount();

        System.out.println("Outstanding Amount: " + outstandingAmount);
        String message = "";
        if(balance.compareTo(repayAmount) < 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            String failure = "Insufficient balance!";
            transaction.setFailureReason(failure);
        }
        else if (repayAmount.compareTo(outstandingAmount) > 0) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            String failure = "You are paying more than required amount.";
            transaction.setFailureReason(failure);
        }
        else{
            account.withdrawal(repayAmount);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            loan.addTransaction(transaction);

            BigDecimal newOutstanding = loan.getOutstandingAmount();
            if(newOutstanding.compareTo(BigDecimal.ONE) < 0) {
                loan.setLoanStatus(LoanStatus.CLOSED);
                message = "Dear " + customer.getName() + ", congratulations! Your loan has been fully repaid. " +
                        "Final payment of " + repayAmount + " processed successfully on " +
                        transaction.getDateOfTransaction().toLocalDate() + ".";
            } else {
                message = "Dear " + customer.getName() + ", your repayment of " + repayAmount +
                        " towards Loan ID " + loan.getId() + " has been successfully processed on " +
                        transaction.getDateOfTransaction().toLocalDate() +
                        ". Remaining outstanding balance: " + newOutstanding + ".";
            }
        }

        if(!message.isEmpty()) notificationService.createNotification(customer, NotificationType.TRANSACTION, message);
        transactionRepostory.save(transaction);
        loanRepository.save(loan);
        accountRepository.save(account);

        return new TransactionResponseDTO(transaction);
    }

    private Employee validateEmployee(String username){
        Employee employee = employeeRepository.findByUsername(username).orElseThrow(EmployeeNotFoundException::new);
        if(!employee.getEmployeeStatus().equals(EmployeeStatus.ACTIVE)) throw new EmployeeInactiveException();
        return employee;
    }

    private BigDecimal getRateOfInterest(LoanType type){
        if(type.equals(LoanType.GOLD)) return BigDecimal.valueOf(7.50);
        else if(type.equals(LoanType.HOME)) return BigDecimal.valueOf(8.40);
        else if(type.equals(LoanType.EDUCATION)) return BigDecimal.valueOf(9.25);
        else if(type.equals(LoanType.VEHICLE)) return BigDecimal.valueOf(8.90);
        else if(type.equals(LoanType.PERSONAL)) return BigDecimal.valueOf(10.50);
        else if(type.equals(LoanType.BUSINESS)) return BigDecimal.valueOf(11.50);
        else throw new IllegalArgumentException();
    }

    public LoanResponseDTO createLoanByCustomer(LoanRequestDTO requestDTO, String username){
        Customer customer = customerRepository.findByUsername(username).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(requestDTO.getAccountId()).orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())) throw new AccountAccessDeniedException();
        Loan loan = createLoan(requestDTO, account);

        String message = "Dear " + customer.getName() + ", your loan application for account A/C "
                + account.getId() + " has been submitted on " + LocalDateTime.now() + ".";
        notificationService.createNotification(customer, NotificationType.ALERT, message);
        return new LoanResponseDTO(loan);
    }

    public Page<LoanResponseDTO> getAllLoansByCustomer(int page, int size, LoanStatus status, LoanType type,
                                                       LocalDate fromDate, LocalDate toDate, String username){
        Customer customer = customerRepository.findByUsername(username).orElseThrow(CustomerNotFoundException::new);
        return getAllLoans(page, size, status, type, fromDate, toDate, customer.getId());
    }

    public TransactionResponseDTO repayLoan(LoanRepaymentDTO repaymentDTO, String username){
        Customer customer = customerRepository.findByUsername(username).orElseThrow(CustomerNotFoundException::new);
        return repayLoan(repaymentDTO, customer);
    }

    public LoanResponseDTO getParticularLoan(Long loanId, String username){
        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);
        if(!loan.getAccount().getCustomer().getUsername().equals(username))
            throw new LoanAccessDeniedException();
        return new LoanResponseDTO(loan);
    }

    public Page<TransactionResponseDTO> getLoanTransactions(Long loanId, int page, int size, TransactionStatus status,
                                                            TransactionType type, LocalDate fromDate,
                                                            LocalDate toDate, String username){
        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);
        if(!loan.getAccount().getCustomer().getUsername().equals(username)) throw new LoanAccessDeniedException();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction"));
        Specification<Transaction> spec = TransactionSpecifications.forLoan(loan)
                .and(TransactionSpecifications.withStatus(status))
                .and(TransactionSpecifications.withType(type))
                .and(TransactionSpecifications.dateBetween(fromDate, toDate));
        Page<Transaction> transactions = transactionRepostory.findAll(spec, pageable);
        return transactions.map(TransactionResponseDTO::new);
    }

    public LoanResponseDTO createLoanByEmployee(LoanRequestDTO requestDTO, Long accountId, String username){
        Employee employee = validateEmployee(username);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        Loan loan = createLoan(requestDTO, account);

        String message = "Dear " + account.getCustomer().getName() + ", your loan application for account A/C "
                + account.getId() + " has been submitted on " + LocalDateTime.now() + ".";
        notificationService.createNotification(account.getCustomer(), NotificationType.ALERT, message);
        return new LoanResponseDTO(loan);
    }

    public Page<LoanResponseDTO> getAllLoansByEmployee(Long customerId, int page, int size, LoanStatus status, LoanType type,
                                                       LocalDate fromDate, LocalDate toDate, String username){
        validateEmployee(username);
        return getAllLoans(page, size, status, type, fromDate, toDate, customerId);
    }

    public LoanResponseDTO getParticularLoanByEmployee(Long loanId, String username){
        validateEmployee(username);
        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);
        return new LoanResponseDTO(loan);
    }

    public Page<TransactionResponseDTO> getLoanTransactionsByEmployee(Long loanId, int page, int size, TransactionStatus status,
                                                            TransactionType type, LocalDate fromDate,
                                                            LocalDate toDate, String username){
        validateEmployee(username);
        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction"));
        Specification<Transaction> spec = TransactionSpecifications.forLoan(loan)
                .and(TransactionSpecifications.withStatus(status))
                .and(TransactionSpecifications.withType(type))
                .and(TransactionSpecifications.dateBetween(fromDate, toDate));
        Page<Transaction> transactions = transactionRepostory.findAll(spec, pageable);
        return transactions.map(TransactionResponseDTO::new);
    }

    public Page<LoanResponseDTO> getAllLoans(int page, int size, LoanStatus status, LoanType type,
                                                       LocalDate fromDate, LocalDate toDate, String username){
        validateEmployee(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfIssuance"));
        Specification<Loan> specification = LoanSpecification
                .withStatus(status)
                .and(LoanSpecification.withType(type))
                .and(LoanSpecification.dateBetween(fromDate, toDate));
        Page<Loan> pageDTO = loanRepository.findAll(specification, pageable);
        return pageDTO.map(LoanResponseDTO::new);
    }
}
