package com.example.bankingapp.dto.transaction;

import com.example.bankingapp.entities.transaction.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequestDTO {
    @NotNull(message = "Date of transaction cannot be null.")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfTransaction;

    @NotNull(message = "Amount cannot be null.")
    private BigDecimal amount;

    private Long fromAccountId;

    private Long toAccountId;

    private Long loanId;

    @NotNull(message = "Transaction type cannot be null.")
    private TransactionType transactionType;

    public LocalDate getDateOfTransaction() {
        return dateOfTransaction;
    }

    public void setDateOfTransaction(LocalDate dateOfTransaction) {
        this.dateOfTransaction = dateOfTransaction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }
}
