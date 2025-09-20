package com.example.bankingapp.controller;

import com.example.bankingapp.dto.transaction.TransactionRequestDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping("customer/transactions/transfer")
    public ResponseEntity<TransactionResponseDTO> transferFund(@RequestBody TransactionRequestDTO requestDTO,
                                                               Principal principal){
        TransactionResponseDTO responseDTO = transactionService.transferFund(requestDTO.getFromAccountId(),
                requestDTO.getToAccountId(), requestDTO.getAmount(), principal.getName());
        URI location = URI.create("/api/transactions/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping("customer/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable Long transactionId, Principal principal){
        TransactionResponseDTO responseDTO = transactionService.getTransaction(transactionId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("employee/transactions/transfer")
    public ResponseEntity<TransactionResponseDTO> transferFundByEmployee(@RequestBody TransactionRequestDTO requestDTO,
                                                               Principal principal){
        TransactionResponseDTO responseDTO = transactionService.transferFundByEmployee(requestDTO.getFromAccountId(),
                requestDTO.getToAccountId(), requestDTO.getAmount(), principal.getName());
        URI location = URI.create("/api/transactions/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("employee/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransactionByEmployee(@PathVariable Long transactionId, Principal principal){
        TransactionResponseDTO responseDTO = transactionService.getTransactionByEmployee(transactionId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }
}
