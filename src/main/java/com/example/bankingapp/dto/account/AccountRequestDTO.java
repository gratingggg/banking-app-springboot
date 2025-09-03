package com.example.bankingapp.dto.account;

import com.example.bankingapp.entities.account.AccountType;
import jakarta.validation.constraints.NotNull;

public class AccountRequestDTO {
    @NotNull(message = "Account type cannot be null.")
    private AccountType accountType;

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
