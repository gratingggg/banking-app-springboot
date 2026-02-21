package com.example.bankingapp.dto.transaction;

import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateOfTransaction;

    private BigDecimal amount;

    private Long fromAccountId;

    private Long toAccountId;

    private Long loanId;

    private Long transactionId;

    private Long accountId;

    private TransactionStatus transactionStatus;

    private TransactionType transactionType;

    private String failureReason;

    private String handledBy;

    private String otherCustomer;

    private String self;

    private boolean isCredit;

    public TransactionResponseDTO(){}

    public TransactionResponseDTO(Transaction transaction, Customer customer){
        if(transaction.getFromAccount() != null) setFromAccountId(transaction.getFromAccount().getId());
        if (transaction.getToAccount() != null) setToAccountId(transaction.getToAccount().getId());
        setTransactionId(transaction.getId());
        setAmount(transaction.getAmount());
        setDateOfTransaction(transaction.getDateOfTransaction());
        if (transaction.getLoan() != null) setLoanId(transaction.getLoan().getId());
        setTransactionStatus(transaction.getTransactionStatus());
        setTransactionType(transaction.getTransactionType());
        if(transaction.getFailureReason() != null) setFailureReason(transaction.getFailureReason());
        if(transaction.getHandledBy() != null) setHandledBy(transaction.getHandledBy().getName());
        if(customer != null){
            setSelf(customer.getName());
            if(transaction.getFromAccount() != null){
                if(transaction.getFromAccount().getCustomer().getId().equals(customer.getId())) setAccountId(getFromAccountId());
                else setOtherCustomer(transaction.getFromAccount().getCustomer().getName());
            }
            if(transaction.getToAccount() != null){
                if(transaction.getToAccount().getCustomer().getId().equals(customer.getId())) setAccountId(getToAccountId());
                else setOtherCustomer(transaction.getToAccount().getCustomer().getName());
            }
            setCredit((transaction.getToAccount() != null) && (transaction.getToAccount().getCustomer().getId().equals(customer.getId())));
        }
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public LocalDateTime getDateOfTransaction() {
        return dateOfTransaction;
    }

    public void setDateOfTransaction(LocalDateTime dateOfTransaction) {
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

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getOtherCustomer() {
        return otherCustomer;
    }

    public void setOtherCustomer(String otherCustomer) {
        this.otherCustomer = otherCustomer;
    }

    public boolean isCredit() {
        return isCredit;
    }

    public void setCredit(boolean credit) {
        isCredit = credit;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }
}
