package com.example.bankingapp.dto.account;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountType;

import java.math.BigDecimal;

public class AccountBalanceResponseDTO {
    private BigDecimal balance;

    private Long accountId;

    private AccountType accountType;

    public AccountBalanceResponseDTO(Account account){
        setBalance(account.getBalance());
        setAccountId(account.getId());
        setAccountType(account.getAccountType());
    }

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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
