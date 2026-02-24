package com.example.bankingapp.service;

import com.example.bankingapp.dto.account.AccountBalanceResponseDTO;
import com.example.bankingapp.dto.account.AccountRequestDTO;
import com.example.bankingapp.dto.account.AccountResponseDTO;
import com.example.bankingapp.dto.account.AccountSummaryDTO;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository,
                          EmployeeRepository employeeRepository,
                          NotificationService notificationService){
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    private AccountResponseDTO accountToAccountDTO(Account account){
        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setAccountId(account.getId());
        responseDTO.setAccountType(account.getAccountType());
        responseDTO.setAccountStatus(account.getAccountStatus());
        responseDTO.setCustomerName(account.getCustomer().getName());
        responseDTO.setDateOfIssuance(account.getDateOfIssuance());
        return responseDTO;
    }

    private List<AccountSummaryDTO> getAllAccounts(Customer customer){
        List<AccountSummaryDTO> listOfDtos = new ArrayList<>();
        for(Account account : customer.getAccounts()){
            AccountSummaryDTO dto = new AccountSummaryDTO(account.getId(), account.getAccountType());
            listOfDtos.add(dto);
        }
        return listOfDtos;
    }

    private Employee validateEmployeeFromUsername(String username){
        Employee employee = employeeRepository.findByUsername(username).orElseThrow(EmployeeNotFoundException::new);
        if(employee.getEmployeeStatus() != EmployeeStatus.ACTIVE){
            throw new EmployeeInactiveException();
        }

        return employee;
    }

    private Account createAccount(AccountRequestDTO requestDTO, Customer customer){
        if(accountRepository.existsByCustomerAndAccountType(customer, requestDTO.getAccountType())){
            throw new AccountDuplicationException();
        }
        Account account = new Account();
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAccountType(requestDTO.getAccountType());
        account.setCustomer(customer);
        account.setDateOfIssuance(LocalDate.now());

        customer.addAccount(account);
        accountRepository.save(account);
        customerRepository.save(customer);

        return account;
    }

    private void deleteAccount(Account account, Customer customer){
        if(account.getAccountStatus().equals(AccountStatus.ACTIVE)){
            if(customer.removeAccount(account)){
                customerRepository.save(customer);
                accountRepository.save(account);
            }
        }
        else throw new AccountNotActiveException();
    }

    private Page<TransactionSummaryDTO> getAllTransactions(Account account, int page, int size, TransactionStatus status,
                                                           TransactionType type, LocalDate fromDate, LocalDate toDate){
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction"));
        Specification<Transaction> specification = TransactionSpecifications.forAccounts(account)
                .and(TransactionSpecifications.withStatus(status))
                .and(TransactionSpecifications.withType(type))
                .and(TransactionSpecifications.dateBetween(fromDate, toDate));;
        Page<Transaction> pageDto = transactionRepository.findAll(specification, pageable);

        return pageDto.map(
                transaction -> new TransactionSummaryDTO(transaction, account.getCustomer())
        );
    }

    private AccountBalanceResponseDTO getBalance(Account account){
        if(account.getAccountStatus().equals(AccountStatus.ACTIVE)){
            return new AccountBalanceResponseDTO(account);
        }
        throw new AccountNotActiveException();
    }

    private void validateFundAndAccount(BigDecimal fund, Account account){
        if(account.getAccountStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException();
        }
        if(fund.compareTo(BigDecimal.ZERO) < 1){
            throw new AmountInvalidException();
        }
    }

    private Transaction getTransaction(BigDecimal fund, Account account){
        Transaction transaction = new Transaction();
        transaction.setDateOfTransaction(LocalDateTime.now());
        transaction.setAmount(fund);
        transaction.setFromAccount(account);
        return transaction;
    }

    public List<AccountSummaryDTO> getAllCustomerAccounts(String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(CustomerNotFoundException::new);
        return getAllAccounts(customer);
    }

    public AccountResponseDTO getParticularCustomerAccount(Long accountId, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())){
            throw new AccountAccessDeniedException("You are not authorized to access this account.");
        }
        return accountToAccountDTO(account);
    }

    public Page<TransactionSummaryDTO> getAllAccountTransactions(Long accountId, int page, int size, TransactionStatus status,
                                                                  TransactionType type, LocalDate fromDate, LocalDate toDate, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())){
            throw new AccountAccessDeniedException("You are not authorized to access this account.");
        }

        return getAllTransactions(account, page, size, status, type, fromDate, toDate);
    }

    @Transactional
    public AccountResponseDTO createAccountByCustomer(AccountRequestDTO requestDTO, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = createAccount(requestDTO, customer);

        String message = "Hello " + customer.getName() +
                "! Your new account (Account No: " + account.getId() + ") has been successfully created." +
                " You can now start using it for deposits, withdrawals, and other banking services.";
        notificationService.createNotification(customer, NotificationType.INFO, message);

        return accountToAccountDTO(account);
    }

    @Transactional
    public AccountResponseDTO deleteAccount(Long accountId, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())){
            throw new AccountAccessDeniedException("You are not authorized to access this account.");
        }

        deleteAccount(account, customer);

        String message = "Hello " + customer.getName() +
                "! Your account (Account No: " + account.getId() + ") has been successfully closed." +
                " If you have any remaining balance or questions, please contact our support.";
        notificationService.createNotification(customer, NotificationType.INFO, message);

        return  accountToAccountDTO(account);
    }

    public AccountBalanceResponseDTO getAccountBalance(Long accountId, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())){
            throw new AccountAccessDeniedException("You are not authorized to access this account.");
        }

        return getBalance(account);
    }

    public List<AccountSummaryDTO> getAllAccountsOfCustomer(Long customerId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        return getAllAccounts(customer);
    }

    public AccountResponseDTO getParticularAccountOfCustomer(Long accountId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return accountToAccountDTO(account);
    }

    @Transactional
    public AccountResponseDTO createCustomerAccountByEmployee(AccountRequestDTO requestDTO, Long customerId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);

        Account account = createAccount(requestDTO, customer);

        String message = "Hello " + customer.getName() +
                "! Your new account (Account No: " + account.getId() + ") has been successfully created." +
                " You can now start using it for deposits, withdrawals, and other banking services.";
        notificationService.createNotification(customer, NotificationType.INFO, message);

        return accountToAccountDTO(account);
    }

    @Transactional
    public AccountResponseDTO deleteCustomerAccountByEmployee(Long accountId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        deleteAccount(account, account.getCustomer());

        String message = "Hello " + account.getCustomer().getName() +
                "! Your account (Account No: " + account.getId() + ") has been successfully closed." +
                " If you have any remaining balance or questions, please contact our support.";
        notificationService.createNotification(account.getCustomer(), NotificationType.INFO, message);

        return accountToAccountDTO(account);
    }

    public Page<TransactionSummaryDTO> getAllAccountTransactionsByEmployee(Long accountId, int page, int size, TransactionStatus status,
                                                                            TransactionType type, LocalDate fromDate, LocalDate toDate, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return getAllTransactions(account, page, size, status, type, fromDate, toDate);
    }

    public AccountBalanceResponseDTO getAccountBalanceByEmployee(Long accountId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return  getBalance(account);
    }

    @Transactional
    public TransactionResponseDTO depositFund(Long accountId, BigDecimal fund, String employeeUsername){
        Employee employee = validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        validateFundAndAccount(fund, account);

        Transaction transaction = getTransaction(fund, account);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setToAccount(account);
        transaction.setFromAccount(null);
        employee.addHandledTransaction(transaction);

        BigDecimal sumAmount = fund;
        Specification<Transaction> specification = TransactionSpecifications.forToAccounts(account)
                .and(TransactionSpecifications.dateBetween(LocalDate.now(), null));
        List<Transaction> listOfTransaction = transactionRepository.findAll(specification);
        for(Transaction tempTransaction : listOfTransaction) {
                if (tempTransaction.isCredit()) {
                    sumAmount = sumAmount.add(tempTransaction.getAmount());
                }
        }

        String message = "";
        if(sumAmount.compareTo(BigDecimal.valueOf(50000)) > 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Daily maximum deposit limit exceeded.");
        }
        else{
            account.deposit(fund);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setFailureReason(null);
            message = "Dear " + account.getCustomer().getName() + ", A/C " + account.getId() +
                    " deposited with " + fund + " on date " + transaction.getDateOfTransaction() + ".";
        }

        transactionRepository.save(transaction);
        accountRepository.save(account);
        employeeRepository.save(employee);

        if(!message.isBlank()){
            notificationService.createNotification(account.getCustomer(), NotificationType.TRANSACTION, message);
        }
        TransactionResponseDTO dto = new TransactionResponseDTO(transaction, null);
        dto.setSelf(account.getCustomer().getName());
        dto.setAccountId(accountId);
        dto.setCredit(true);

        return dto;
    }

    @Transactional
    public TransactionResponseDTO withdrawFund(Long accountId, BigDecimal fund, String employeeUsername){
        Employee employee = validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        validateFundAndAccount(fund, account);

        Transaction transaction = getTransaction(fund, account);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        employee.addHandledTransaction(transaction);

        BigDecimal sumAmount = fund;
        Specification<Transaction> specification = TransactionSpecifications.forFromAccounts(account)
                .and(TransactionSpecifications.dateBetween(LocalDate.now(), null));
        List<Transaction> listOfTransaction = transactionRepository.findAll(specification);
        for(Transaction tempTransaction : listOfTransaction) {
                if (tempTransaction.isDebit() || tempTransaction.getTransactionType().equals(TransactionType.TRANSFERRED)) {
                    sumAmount = sumAmount.add(tempTransaction.getAmount());
                }
        }

        String message = "";
        if(fund.compareTo(account.getBalance()) > 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Insufficient balance.");
        }
        else if(sumAmount.compareTo(BigDecimal.valueOf(50000)) > 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Daily maximum withdraw limit exceeded.");
        }
        else{
            account.withdrawal(fund);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setFailureReason(null);
            message = "Dear " + account.getCustomer().getName() + ", A/C " + account.getId() +
                    " has a withdrawal of " + fund + " on date " + transaction.getDateOfTransaction() + ".";
        }

        transactionRepository.save(transaction);
        accountRepository.save(account);
        employeeRepository.save(employee);

        if(!message.isBlank()){
            notificationService.createNotification(account.getCustomer(), NotificationType.TRANSACTION, message);
        }

        TransactionResponseDTO dto = new TransactionResponseDTO(transaction, null);
        dto.setSelf(account.getCustomer().getName());
        dto.setAccountId(accountId);
        dto.setCredit(false);

        return dto;
    }
}