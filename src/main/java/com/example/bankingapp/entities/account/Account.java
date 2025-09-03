package com.example.bankingapp.entities.account;

import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.baseentities.BaseEntity;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
    @Column(name = "type", nullable = false)
    @NotNull(message = "Account type cannot be null")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Account status cannot be null")
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Column(name = "balance",nullable = false)
    @NotNull(message = "Balance cannot be null")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "date",nullable = false)
    @NotNull(message = "Date of issuance cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "customer cannot be null")
    private Customer customer;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private final List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private final Set<Transaction> transactions = new LinkedHashSet<>();

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public synchronized void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDate getDateOfIssuance() {
        return dateOfIssuance;
    }

    public void setDateOfIssuance(LocalDate dateOfIssuance) {
        this.dateOfIssuance = dateOfIssuance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void addLoan(Loan loan){
        loans.add(loan);
        loan.setAccount(this);;
    }

    public synchronized void addTransaction(Transaction transaction){
        transactions.add(transaction);
        transaction.setAccount(this);
    }

    public synchronized BigDecimal deposit(BigDecimal depositFund){
        if(depositFund != null){
            if(depositFund.compareTo(BigDecimal.ZERO) > 0){
                balance = balance.add(depositFund);
                return balance;
            }
            throw new IllegalArgumentException("Deposit amount cannot be zero or negative.");
        }
        throw new IllegalArgumentException("Deposit cannot be null.");
    }

    public synchronized BigDecimal withdrawal(BigDecimal withdrawalAmount){
        if(withdrawalAmount != null){
            if(withdrawalAmount.compareTo(BigDecimal.ZERO) > 0){
                if (balance.compareTo(withdrawalAmount) < 0) {
                    throw new IllegalArgumentException("Insufficient balance.");
                }
                balance = balance.subtract(withdrawalAmount);
                return balance;
            }
            throw new IllegalArgumentException("Withdrawal amount cannot be zero or negative.");
        }
        throw new IllegalArgumentException("Withdrawal amount cannot be null.");
    }

    public synchronized BigDecimal transferTo(Account account, BigDecimal amount){
        if(account != null){
            if(account.getAccountStatus() == AccountStatus.ACTIVE){
                this.withdrawal(amount);
                account.deposit(amount);
                return this.balance;
            }
            throw new IllegalArgumentException("The account is not active.");
        }
        throw new IllegalArgumentException("Account cannot be null.");
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("id : ", getId())
                .append("type : ", getAccountType())
                .append("status : ", getAccountStatus())
                .append("balance", getBalance())
                .append("issuance date : " , getDateOfIssuance())
                .toString();
    }
}
