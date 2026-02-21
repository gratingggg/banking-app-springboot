package com.example.bankingapp.controller;

import com.example.bankingapp.dto.account.AccountBalanceResponseDTO;
import com.example.bankingapp.dto.account.AccountRequestDTO;
import com.example.bankingapp.dto.account.AccountResponseDTO;
import com.example.bankingapp.dto.account.AccountSummaryDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionSummaryDTO;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.service.AccountService;
import com.example.bankingapp.utils.Endpoints;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @GetMapping(Endpoints.CUSTOMER_ACCOUNTS_ALL)
    public ResponseEntity<List<AccountSummaryDTO>> getAllCustomerAccounts(Principal principal){
        List<AccountSummaryDTO> listOfDto = accountService.getAllCustomerAccounts(principal.getName());
        return ResponseEntity.ok(listOfDto);
    }

    @GetMapping(Endpoints.CUSTOMER_ACCOUNT_PARTICULAR)
    public ResponseEntity<AccountResponseDTO> getParticularCustomerAccount(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.getParticularCustomerAccount(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(Endpoints.CUSTOMER_ACCOUNT_TRANSACTION_ALL)
    public ResponseEntity<Page<TransactionSummaryDTO>> getAllAccountTransaction(@PathVariable Long accountId,
                                                                                @RequestParam(defaultValue = "0", required = false) int page,
                                                                                @RequestParam(defaultValue = "10", required = false) int size,
                                                                                @RequestParam(required = false) TransactionStatus status,
                                                                                @RequestParam(required = false) TransactionType type,
                                                                                @RequestParam(required = false) LocalDate fromDate,
                                                                                @RequestParam(required = false) LocalDate toDate,
                                                                                Principal principal){
        Page<TransactionSummaryDTO> responseDTO = accountService.getAllAccountTransactions(accountId, page, size,
                status, type, fromDate, toDate,principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(Endpoints.CUSTOMER_ACCOUNT_CREATE)
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountRequestDTO requestDTO, Principal principal){
        AccountResponseDTO responseDTO = accountService.createAccountByCustomer(requestDTO, principal.getName());
        URI location = URI.create("/api/accounts/" + responseDTO.getAccountId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(Endpoints.CUSTOMER_ACCOUNT_DELETE)
    public ResponseEntity<AccountResponseDTO> deleteAccountByCustomer(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.deleteAccount(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(Endpoints.CUSTOMER_ACCOUNT_BALANCE)
    public ResponseEntity<AccountBalanceResponseDTO> getAccountBalance(@PathVariable Long accountId, Principal principal){
        AccountBalanceResponseDTO responseDTO = accountService.getAccountBalance(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(Endpoints.EMPLOYEE_ACCOUNTS_ALL)
    public ResponseEntity<List<AccountSummaryDTO>> getAllAccountsOfCustomer(@PathVariable Long customerId, Principal principal){
        List<AccountSummaryDTO> summaryDTOS = accountService.getAllAccountsOfCustomer(customerId, principal.getName());
        return ResponseEntity.ok(summaryDTOS);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(Endpoints.EMPLOYEE_ACCOUNT_PARTICULAR)
    public ResponseEntity<AccountResponseDTO> getParticularAccountOfCustomer(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.getParticularAccountOfCustomer(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping(Endpoints.EMPLOYEE_ACCOUNT_CREATE)
    public ResponseEntity<AccountResponseDTO> createCustomerAccountByEmployee(@RequestBody AccountRequestDTO requestDTO, @PathVariable Long customerId, Principal principal){
        AccountResponseDTO responseDTO = accountService.createCustomerAccountByEmployee(requestDTO, customerId, principal.getName());
        URI location = URI.create("/api/accounts/" + responseDTO.getAccountId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping(Endpoints.EMPLOYEE_ACCOUNT_DELETE)
    public ResponseEntity<AccountResponseDTO> deleteCustomerAccountByEmployee(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.deleteCustomerAccountByEmployee(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(Endpoints.EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL)
    public ResponseEntity<Page<TransactionSummaryDTO>> getAllTransactionsOfAccountByEmployee(@PathVariable Long accountId,
                                                                                              @RequestParam(required = false, defaultValue = "0") int page,
                                                                                              @RequestParam(required = false, defaultValue = "10") int size,
                                                                                              @RequestParam(required = false) TransactionStatus status,
                                                                                              @RequestParam(required = false) TransactionType type,
                                                                                              @RequestParam(required = false) LocalDate fromDate,
                                                                                              @RequestParam(required = false) LocalDate toDate,
                                                                                              Principal principal){
        Page<TransactionSummaryDTO> responseDTOS = accountService.getAllAccountTransactionsByEmployee(accountId, page, size, status, type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(responseDTOS);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(Endpoints.EMPLOYEE_ACCOUNT_BALANCE)
    public ResponseEntity<AccountBalanceResponseDTO> getAccountBalanceByEmployee(@PathVariable Long accountId, Principal principal){
        AccountBalanceResponseDTO responseDTO = accountService.getAccountBalanceByEmployee(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping(Endpoints.EMPLOYEE_ACCOUNT_DEPOSIT)
    public ResponseEntity<TransactionResponseDTO> depositFund(@PathVariable Long accountId,
                                                              @Valid @RequestParam BigDecimal fund,
                                                              Principal principal){
        TransactionResponseDTO responseDTO = accountService.depositFund(accountId, fund, principal.getName());
        URI location = URI.create("/api/transaction/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping(Endpoints.EMPLOYEE_ACCOUNT_WITHDRAWAL)
    public ResponseEntity<TransactionResponseDTO> withdrawFund(@PathVariable Long accountId,
                                                               @Valid @RequestParam BigDecimal fund,
                                                               Principal principal){
        TransactionResponseDTO responseDTO = accountService.withdrawFund(accountId, fund, principal.getName());
        URI location = URI.create("/api/transaction/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }
}
