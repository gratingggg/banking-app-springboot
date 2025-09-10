package com.example.bankingapp.exception;

public class AccountBalanceNotZeroException extends RuntimeException {
    public AccountBalanceNotZeroException(String message) {
        super(message);
    }

    public AccountBalanceNotZeroException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountBalanceNotZeroException() {
        super("Account balance is not zero. Please withdraw the remaining money before deleting the account.");
    }
}
