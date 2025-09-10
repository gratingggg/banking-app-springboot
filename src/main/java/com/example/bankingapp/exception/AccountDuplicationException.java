package com.example.bankingapp.exception;

public class AccountDuplicationException extends RuntimeException {
    public AccountDuplicationException(String message) {
        super(message);
    }

    public AccountDuplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountDuplicationException() {
        super("The account with given type already exists.");
    }
}
