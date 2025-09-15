package com.example.bankingapp.controller;

import com.example.bankingapp.dto.account.AccountBalanceResponseDTO;
import com.example.bankingapp.dto.account.AccountRequestDTO;
import com.example.bankingapp.dto.account.AccountResponseDTO;
import com.example.bankingapp.dto.account.AccountSummaryDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @GetMapping("/customer/accounts")
    public ResponseEntity<List<AccountSummaryDTO>> getAllCustomerAccounts(Principal principal){
        List<AccountSummaryDTO> listOfDto = accountService.getAllCustomerAccounts(principal.getName());
        return ResponseEntity.ok(listOfDto);
    }

    @GetMapping("/customer/accounts/{accountId}")
    public ResponseEntity<AccountResponseDTO> getParticularCustomerAccount(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.getParticularCustomerAccount(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/customer/accounts/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getAllAccountTransaction(@PathVariable Long accountId, @RequestParam(defaultValue = "0", required = false) int page,
                                                                          @RequestParam(defaultValue = "10", required = false) int size, Principal principal){
        Page<TransactionResponseDTO> responseDTO = accountService.getAllAccountTransactions(accountId, page, size, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/customer/accounts")
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountRequestDTO requestDTO, Principal principal){
        AccountResponseDTO responseDTO = accountService.createAccountByCustomer(requestDTO, principal.getName());
        URI location = URI.create("/api/accounts/" + responseDTO.getAccountId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/customer/accounts/{accountId}/close")
    public ResponseEntity<AccountResponseDTO> deleteAccountByCustomer(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.deleteAccount(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/customer/accounts/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponseDTO> getAccountBalance(@PathVariable Long accountId, Principal principal){
        AccountBalanceResponseDTO responseDTO = accountService.getAccountBalance(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/customer/{customerId}/accounts")
    public ResponseEntity<List<AccountSummaryDTO>> getAllAccountsOfCustomer(@PathVariable Long customerId, Principal principal){
        List<AccountSummaryDTO> summaryDTOS = accountService.getAllAccountsOfCustomer(customerId, principal.getName());
        return ResponseEntity.ok(summaryDTOS);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/accounts/{accountId}")
    public ResponseEntity<AccountResponseDTO> getParticularAccountOfCustomer(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.getParticularAccountOfCustomer(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/employee/customer/{customerId}/accounts")
    public ResponseEntity<AccountResponseDTO> createCustomerAccountByEmployee(@RequestBody AccountRequestDTO requestDTO, @PathVariable Long customerId, Principal principal){
        AccountResponseDTO responseDTO = accountService.createCustomerAccountByEmployee(requestDTO, customerId, principal.getName());
        URI location = URI.create("/api/accounts/" + responseDTO.getAccountId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/employee/accounts/{accountId}/close")
    public ResponseEntity<AccountResponseDTO> deleteCustomerAccountByEmployee(@PathVariable Long accountId, Principal principal){
        AccountResponseDTO responseDTO = accountService.deleteCustomerAccountByEmployee(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/accounts/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getAllTransactionsOfAccountByEmployee(@PathVariable Long accountId,
                                                                                              @RequestParam(required = false, defaultValue = "0") int page,
                                                                                              @RequestParam(required = false, defaultValue = "10") int size,
                                                                                              Principal principal){
        Page<TransactionResponseDTO> responseDTOS = accountService.getAllAccountTransactionsByEmployee(accountId, page, size, principal.getName());
        return ResponseEntity.ok(responseDTOS);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/accounts/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponseDTO> getAccountBalanceByEmployee(@PathVariable Long accountId, Principal principal){
        AccountBalanceResponseDTO responseDTO = accountService.getAccountBalanceByEmployee(accountId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/customer/{customerId}/accounts/transactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getAllTransactionsOfCustomer(@PathVariable Long customerId,
                                                                                     @RequestParam(required = false, defaultValue = "0") int page,
                                                                                     @RequestParam(required = false, defaultValue = "10") int size,
                                                                                     Principal principal){
        Page<TransactionResponseDTO> responseDTOS = accountService.getAllTransactionsOfCustomer(customerId, page, size, principal.getName());
        return ResponseEntity.ok(responseDTOS);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/employee/accounts/{accountId}/deposit")
    public ResponseEntity<TransactionResponseDTO> depositFund(@PathVariable Long accountId,
                                                              @Valid @RequestParam(required = true)BigDecimal fund,
                                                              Principal principal){
        TransactionResponseDTO responseDTO = accountService.depositFund(accountId, fund, principal.getName());
        URI location = URI.create("/api/transaction/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/employee/accounts/{accountId}/withdrawal")
    public ResponseEntity<TransactionResponseDTO> withdrawFund(@PathVariable Long accountId,
                                                               @Valid @RequestParam(required = true) BigDecimal fund,
                                                               Principal principal){
        TransactionResponseDTO responseDTO = accountService.withdrawFund(accountId, fund, principal.getName());
        URI location = URI.create("/api/transaction/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }


}
