package com.example.bankingapp.dto.transaction;

import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionSummaryDTO {
    private Long transactionId;

    private String otherCustomer;

    private LocalDate dateOfTransaction;

    private BigDecimal amount;

    private boolean isCredit;

    private boolean success;

    public TransactionSummaryDTO(Transaction transaction, Customer customer){
        setTransactionId(transaction.getId());
        if(customer != null){
            setCredit((transaction.getToAccount() != null) && transaction.getToAccount().getCustomer().getId().equals(customer.getId()));
        }
        if(isCredit() && transaction.getFromAccount() != null) setOtherCustomer(transaction.getFromAccount().getCustomer().getName());
        else if(!isCredit() && transaction.getToAccount() != null) setOtherCustomer(transaction.getToAccount().getCustomer().getName());
        setDateOfTransaction(transaction.getDateOfTransaction().toLocalDate());
        setAmount(transaction.getAmount());
        setSuccess(transaction.getTransactionStatus().equals(TransactionStatus.SUCCESS));
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getOtherCustomer() {
        return otherCustomer;
    }

    public void setOtherCustomer(String otherCustomer) {
        this.otherCustomer = otherCustomer;
    }

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

    public boolean isCredit() {
        return isCredit;
    }

    public void setCredit(boolean credit) {
        isCredit = credit;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
