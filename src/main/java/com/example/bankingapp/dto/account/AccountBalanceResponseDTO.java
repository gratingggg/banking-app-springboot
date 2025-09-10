package com.example.bankingapp.dto.account;

import java.math.BigDecimal;

public class AccountBalanceResponseDTO {
    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
