package com.example.bankingapp.service;

import com.example.bankingapp.dto.account.AccountBalanceResponseDTO;
import com.example.bankingapp.dto.account.AccountRequestDTO;
import com.example.bankingapp.dto.account.AccountResponseDTO;
import com.example.bankingapp.dto.account.AccountSummaryDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.exception.*;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.CustomerRepository;
import com.example.bankingapp.repository.EmployeeRepository;
import com.example.bankingapp.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final EmployeeRepository employeeRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository,
                          EmployeeRepository employeeRepository){
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.employeeRepository = employeeRepository;
    }

    private AccountResponseDTO accountToAccountDTO(Account account){
        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setAccountId(account.getId());
        responseDTO.setAccountType(account.getAccountType());
        responseDTO.setAccountStatus(account.getAccountStatus());
        responseDTO.setBalance(account.getBalance());
        responseDTO.setCustomerName(account.getCustomer().getName());
        responseDTO.setDateOfIssuance(account.getDateOfIssuance());
        return responseDTO;
    }

    private TransactionResponseDTO transactionToDto(Transaction transaction){
        TransactionResponseDTO dto = new TransactionResponseDTO();
        if(transaction.getFromAccount() != null) dto.setFromAccountId(transaction.getFromAccount().getId());
        if (transaction.getToAccount() != null) dto.setToAccountId(transaction.getToAccount().getId());
        dto.setTransactionId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setDateOfTransaction(transaction.getDateOfTransaction());
        if (transaction.getLoan() != null) dto.setLoanId(transaction.getLoan().getId());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setFailureReason(transaction.getFailureReason());

        return dto;
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

    private Page<TransactionResponseDTO> getAllTransactions(Account fromAccount, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction"));
        Page<Transaction> pageDto = transactionRepository.findByAccount(fromAccount, pageable);

        return pageDto.map(this::transactionToDto);
    }

    private AccountBalanceResponseDTO getBalance(Account account){
        if(account.getAccountStatus().equals(AccountStatus.ACTIVE)){
            AccountBalanceResponseDTO responseDTO = new AccountBalanceResponseDTO();
            responseDTO.setBalance(account.getBalance());
            return responseDTO;
        }
        throw new AccountNotActiveException();
    }

    private void validateFundAndAccount(BigDecimal fund, Account account){
        if(account.getAccountStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException();
        }
        if(fund.compareTo(BigDecimal.ZERO) < 1){
            throw new TransactionAmountInvalidException();
        }
    }

    private Transaction getTransaction(BigDecimal fund, Account account){
        Transaction transaction = new Transaction();
        transaction.setDateOfTransaction(LocalDate.now());
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

    public Page<TransactionResponseDTO> getAllAccountTransactions(Long accountId, int page, int size, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())){
            throw new AccountAccessDeniedException("You are not authorized to access this account.");
        }

        return getAllTransactions(account, page, size);
    }

    public AccountResponseDTO createAccountByCustomer(AccountRequestDTO requestDTO, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = createAccount(requestDTO, customer);
        return accountToAccountDTO(account);
    }

    public AccountResponseDTO deleteAccount(Long accountId, String customerUsername){
        Customer customer = customerRepository.findByUsername(customerUsername).orElseThrow(CustomerNotFoundException::new);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        if(!account.getCustomer().getId().equals(customer.getId())){
            throw new AccountAccessDeniedException("You are not authorized to access this account.");
        }

        deleteAccount(account, customer);
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

    public AccountResponseDTO createCustomerAccountByEmployee(AccountRequestDTO requestDTO, Long customerId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        return accountToAccountDTO(createAccount(requestDTO, customer));
    }

    public AccountResponseDTO deleteCustomerAccountByEmployee(Long accountId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        deleteAccount(account, account.getCustomer());
        return accountToAccountDTO(account);
    }

    public Page<TransactionResponseDTO> getAllAccountTransactionsByEmployee(Long accountId, int page,
                                                                           int size, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return getAllTransactions(account, page, size);
    }

    public AccountBalanceResponseDTO getAccountBalanceByEmployee(Long accountId, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return  getBalance(account);
    }

    public Page<TransactionResponseDTO> getAllTransactionsOfCustomer(Long customerId, int page, int size, String employeeUsername){
        validateEmployeeFromUsername(employeeUsername);
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction"));
        Page<Transaction> transactions = transactionRepository.findByCustomer(customer, pageable);
        return transactions.map(this::transactionToDto);
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
        List<Transaction> listOfTransaction = transactionRepository.findByAccount(account);
        for(Transaction tempTransaction : listOfTransaction) {
            if(tempTransaction.getDateOfTransaction().equals(LocalDate.now())){
                if (tempTransaction.isCredit() && tempTransaction.getTransactionStatus().equals(TransactionStatus.SUCCESS)) {
                    sumAmount = sumAmount.add(tempTransaction.getAmount());
                }
            }
        }

        if(sumAmount.compareTo(BigDecimal.valueOf(50000)) > 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Daily maximum deposit limit exceeded.");
        }
        else{
            account.deposit(fund);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setFailureReason(null);
        }

        transactionRepository.save(transaction);
        accountRepository.save(account);
        employeeRepository.save(employee);

        return transactionToDto(transaction);
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
        List<Transaction> listOfTransaction = transactionRepository.findByAccount(account);
        for(Transaction tempTransaction : listOfTransaction) {
            if(tempTransaction.getDateOfTransaction().equals(LocalDate.now())){
                if (tempTransaction.isDebit() && tempTransaction.getTransactionStatus().equals(TransactionStatus.SUCCESS)) {
                    sumAmount = sumAmount.add(tempTransaction.getAmount());
                }
            }
        }

        if(sumAmount.compareTo(BigDecimal.valueOf(50000)) > 0){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Daily maximum withdraw limit exceeded.");
        }
        else{
            account.withdrawal(fund);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setFailureReason(null);
        }

        transactionRepository.save(transaction);
        accountRepository.save(account);
        employeeRepository.save(employee);

        return transactionToDto(transaction);
    }
}