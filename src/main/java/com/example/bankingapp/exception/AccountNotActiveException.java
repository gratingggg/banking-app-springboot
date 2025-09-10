package com.example.bankingapp.exception;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(String message) {
        super(message);
    }

    public AccountNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNotActiveException() {
        super("Your account is currently not active.");
    }
}
