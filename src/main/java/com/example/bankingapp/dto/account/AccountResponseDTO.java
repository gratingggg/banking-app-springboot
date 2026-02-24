package com.example.bankingapp.dto.account;

import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.account.AccountType;
import com.example.bankingapp.utils.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class AccountResponseDTO {
    private Long accountId;

    private AccountType accountType;

    private AccountStatus accountStatus;

    @JsonFormat(pattern = Constants.LocalDatePattern)
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
