package com.example.bankingapp.account;

import com.example.bankingapp.customer.Customer;
import com.example.bankingapp.entities.BaseEntity;
import com.example.bankingapp.loan.Loan;
import com.example.bankingapp.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

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
    private Long balance = 0L;

    @Column(name = "date",nullable = false)
    @NotNull(message = "Date of issuance cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "customer cannot be null")
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    public void setStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
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

    public void addTransaction(Transaction transaction){
        transactions.add(transaction);
        transaction.setAccount(this);
    }
}
