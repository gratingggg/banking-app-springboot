package com.example.bankingapp.account;

import com.example.bankingapp.customer.Customer;
import com.example.bankingapp.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

enum AccountType{
    SAVINGS,
    CURRENT
}

enum Status{
    ACTIVE,
    INACTIVE,
    CLOSED
}

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
    @Column(name = "type")
    @NotNull(message = "Account type cannot be null")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "status")
    @NotNull(message = "Account status cannot be null")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "balance")
    @NotNull(message = "Balance cannot be null")
    private Long balance = 0L;

    @Column(name = "date")
    @NotNull(message = "Date of issuance cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
}
