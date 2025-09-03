package com.example.bankingapp.transaction;

import com.example.bankingapp.account.Account;
import com.example.bankingapp.employee.Employee;
import com.example.bankingapp.entities.BaseEntity;
import com.example.bankingapp.loan.Loan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    @Column(name = "date", nullable = false)
    @NotNull(message = "Date of transaction cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfTransaction;

    @Column(name = "amount", nullable = false)
    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Account cannot be null")
    private Account account;

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
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee cannot be null")
    private Employee handledBy;

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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
