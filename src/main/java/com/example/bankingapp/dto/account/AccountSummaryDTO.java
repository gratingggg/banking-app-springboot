package com.example.bankingapp.dto.account;

import com.example.bankingapp.entities.account.AccountType;

public class AccountSummaryDTO {
    private Long accountId;
    private AccountType accountType;

    public AccountSummaryDTO(){}

    public AccountSummaryDTO(Long accountId, AccountType accountType) {
        this.accountId = accountId;
        this.accountType = accountType;
    }

    public Long getAccountId() {
        return accountId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

}
