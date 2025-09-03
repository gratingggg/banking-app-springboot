package com.example.bankingapp.dto.account;

import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.account.AccountType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountResponseDTO {
    private Long accountId;

    private AccountType accountType;

    private AccountStatus accountStatus;

    private BigDecimal balance ;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    private String customerName;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

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

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDate getDateOfIssuance() {
        return dateOfIssuance;
    }

    public void setDateOfIssuance(LocalDate dateOfIssuance) {
        this.dateOfIssuance = dateOfIssuance;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


}
