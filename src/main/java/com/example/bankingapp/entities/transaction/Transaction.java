package com.example.bankingapp.entities.transaction;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.baseentities.BaseEntity;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.loan.Loan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    @Column(name = "date", nullable = false)
    @NotNull(message = "Date of transaction cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime dateOfTransaction;

    @Column(name = "amount", nullable = false)
    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @ManyToOne
    @JoinColumn(name = "Loan_id")
    private Loan loan;

    @Column(name = "type", nullable = false)
    @NotNull(message = "Transaction type cannot be null")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Transaction status cannot be null")
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee handledBy;

    @Column(name = "failure_reasons")
    private String failureReason;

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

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Employee getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Employee handledBy) {
        this.handledBy = handledBy;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public boolean isDebit(){
        if(this.getTransactionType() == TransactionType.WITHDRAWAL ||
                getTransactionType() == TransactionType.CHARGE ||
                getTransactionType() == TransactionType.LOAN_REPAYMENT){
            return this.getTransactionStatus() == TransactionStatus.SUCCESS;
        }
        return false;
    }

    public boolean isCredit(){
        if(getTransactionType() == TransactionType.DEPOSIT ||
                getTransactionType() == TransactionType.INTEREST ||
                getTransactionType() == TransactionType.LOAN_DISBURSEMENT){
            return this.getTransactionStatus() == TransactionStatus.SUCCESS;
        }
        return false;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("id : ", getId())
                .append("date : ", getDateOfTransaction())
                .append("amount : ", getAmount())
                .append("type : ", getTransactionType())
                .append("status : ", getTransactionStatus())
                .append("handled by : ", getHandledBy().getName())
                .toString();
    }
}
