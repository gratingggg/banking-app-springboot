package com.example.bankingapp.controller;

import com.example.bankingapp.dto.transaction.TransactionRequestDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionSummaryDTO;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.service.TransactionService;
import com.example.bankingapp.utils.Endpoints;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;

@RestController
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping(Endpoints.TRANSACTIONS_CUSTOMER_TRANSFER)
    public ResponseEntity<TransactionResponseDTO> transferFund(@RequestBody TransactionRequestDTO requestDTO,
                                                               Principal principal){
        TransactionResponseDTO responseDTO = transactionService.transferFund(requestDTO.getFromAccountId(),
                requestDTO.getToAccountId(), requestDTO.getAmount(), principal.getName());
        URI location = URI.create("/api/transactions/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping(Endpoints.TRANSACTION_CUSTOMER)
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable Long transactionId, Principal principal){
        TransactionResponseDTO responseDTO = transactionService.getTransaction(transactionId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(Endpoints.CUSTOMER_TRANSACTION_ALL)
    public ResponseEntity<Page<TransactionSummaryDTO>> getAllTransactionsByCustomer(@RequestParam(required = false, defaultValue = "0") int page,
                                                                                    @RequestParam(required = false, defaultValue = "10") int size,
                                                                                    @RequestParam(required = false) TransactionStatus status,
                                                                                    @RequestParam(required = false) TransactionType type,
                                                                                    @RequestParam(required = false) LocalDate fromDate,
                                                                                    @RequestParam(required = false) LocalDate toDate,
                                                                                    Principal principal) {
        Page<TransactionSummaryDTO> responseDTOS = transactionService.getAllTransactionsByCustomer(page, size, status, type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(responseDTOS);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping(Endpoints.TRANSACTIONS_EMPLOYEE_TRANSFER)
    public ResponseEntity<TransactionResponseDTO> transferFundByEmployee(@RequestBody TransactionRequestDTO requestDTO,
                                                               Principal principal){
        TransactionResponseDTO responseDTO = transactionService.transferFundByEmployee(requestDTO.getFromAccountId(),
                requestDTO.getToAccountId(), requestDTO.getAmount(), principal.getName());
        URI location = URI.create("/api/transactions/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(Endpoints.TRANSACTION_EMPLOYEE)
    public ResponseEntity<TransactionResponseDTO> getTransactionByEmployee(@PathVariable Long transactionId, Principal principal){
        TransactionResponseDTO responseDTO = transactionService.getTransactionByEmployee(transactionId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(Endpoints.EMPLOYEE_CUSTOMER_TRANSACTION_ALL)
    public ResponseEntity<Page<TransactionSummaryDTO>> getAllTransactionsOfCustomer(@PathVariable Long customerId,
                                                                                     @RequestParam(required = false, defaultValue = "0") int page,
                                                                                     @RequestParam(required = false, defaultValue = "10") int size,
                                                                                     @RequestParam(required = false) TransactionStatus status,
                                                                                     @RequestParam(required = false) TransactionType type,
                                                                                     @RequestParam(required = false) LocalDate fromDate,
                                                                                     @RequestParam(required = false) LocalDate toDate,
                                                                                     Principal principal){
        Page<TransactionSummaryDTO> responseDTOS = transactionService.getAllTransactionsOfCustomer(customerId, page, size,
                status, type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(responseDTOS);
    }
}
