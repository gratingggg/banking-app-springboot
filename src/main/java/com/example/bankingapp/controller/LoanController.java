package com.example.bankingapp.controller;

import com.example.bankingapp.dto.loan.LoanRepaymentDTO;
import com.example.bankingapp.dto.loan.LoanRequestDTO;
import com.example.bankingapp.dto.loan.LoanResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.example.bankingapp.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class LoanController{
    private final LoanService loanService;

    @Autowired
    public LoanController(LoanService loanService){
        this.loanService = loanService;
    }

    @PostMapping("/customer/loan/apply")
    public ResponseEntity<LoanResponseDTO> createLoanByCustomer(@RequestBody LoanRequestDTO requestDTO, Principal principal){
        LoanResponseDTO responseDTO = loanService.createLoanByCustomer(requestDTO, principal.getName());
        URI location = URI.create("/api/loan/" + responseDTO.getLoanId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping("/customer/loan")
    public ResponseEntity<Page<LoanResponseDTO>> getAllLoansByCustomer(@RequestParam(defaultValue = "0", required = false) int page,
                                                                       @RequestParam(defaultValue = "10", required = false) int size,
                                                                       @RequestParam(required = false)LoanStatus status,
                                                                       @RequestParam(required = false)LoanType type,
                                                                       @RequestParam(required = false) LocalDate fromDate,
                                                                       @RequestParam(required = false)LocalDate toDate,
                                                                       Principal principal){
        Page<LoanResponseDTO> responseDTO = loanService.getAllLoansByCustomer(page, size, status, type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/customer/loan/repay")
    public ResponseEntity<TransactionResponseDTO> repayLoan(@RequestBody LoanRepaymentDTO repaymentDTO, Principal principal){
        TransactionResponseDTO responseDTO = loanService.repayLoan(repaymentDTO, principal.getName());
        URI location = URI.create("/api/transaction/" + responseDTO.getTransactionId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping("/customer/loan/{loanId}")
    public ResponseEntity<LoanResponseDTO> getParticularLoan(@PathVariable Long loanId, Principal principal){
        LoanResponseDTO responseDTO = loanService.getParticularLoan(loanId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/customer/loan/{loanId}/transactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getLoanTransactions(@PathVariable Long loanId,
                                                                            @RequestParam(defaultValue = "0", required = false) int page,
                                                                            @RequestParam(defaultValue = "10", required = false) int size,
                                                                            @RequestParam(required = false) TransactionStatus status,
                                                                            @RequestParam(required = false) TransactionType type,
                                                                            @RequestParam(required = false) LocalDate fromDate,
                                                                            @RequestParam(required = false) LocalDate toDate,
                                                                            Principal principal){
        Page<TransactionResponseDTO> response = loanService.getLoanTransactions(loanId, page, size, status,
                type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/employee/customer/account/{accountId}/loan/apply")
    public ResponseEntity<LoanResponseDTO> createLoanByEmployee(@RequestBody LoanRequestDTO requestDTO,
                                                                @PathVariable Long accountId,
                                                                Principal principal){
        LoanResponseDTO responseDTO = loanService.createLoanByEmployee(requestDTO, accountId, principal.getName());
        URI location = URI.create("/api/loan/" + responseDTO.getLoanId());
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/customer/{customerId}/loan")
    public ResponseEntity<Page<LoanResponseDTO>> getAllLoansByEmployee(@PathVariable Long customerId,
                                                                            @RequestParam(defaultValue = "0", required = false) int page,
                                                                            @RequestParam(defaultValue = "10", required = false) int size,
                                                                            @RequestParam(required = false)LoanStatus status,
                                                                            @RequestParam(required = false)LoanType type,
                                                                            @RequestParam(required = false) LocalDate fromDate,
                                                                            @RequestParam(required = false)LocalDate toDate,
                                                                            Principal principal){
        Page<LoanResponseDTO> responseDTO = loanService.getAllLoansByEmployee(customerId, page, size, status, type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("employee/customer/loan/{loanId}")
    public ResponseEntity<LoanResponseDTO> getParticularLoanByEmployee(@PathVariable Long loanId, Principal principal){
        LoanResponseDTO responseDTO = loanService.getParticularLoanByEmployee(loanId, principal.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("employee/customer/loan/{loanId}/transactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getLoanTransactionsByEmployee(@PathVariable Long loanId,
                                                                            @RequestParam(defaultValue = "0", required = false) int page,
                                                                            @RequestParam(defaultValue = "10", required = false) int size,
                                                                            @RequestParam(required = false) TransactionStatus status,
                                                                            @RequestParam(required = false) TransactionType type,
                                                                            @RequestParam(required = false) LocalDate fromDate,
                                                                            @RequestParam(required = false) LocalDate toDate,
                                                                            Principal principal){
        Page<TransactionResponseDTO> response = loanService.getLoanTransactionsByEmployee(loanId, page, size, status,
                type, fromDate, toDate, principal.getName());
        return ResponseEntity.ok(response);
    }
}
