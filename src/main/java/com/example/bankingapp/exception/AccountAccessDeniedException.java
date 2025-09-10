package com.example.bankingapp.exception;

public class AccountAccessDeniedException extends RuntimeException {
    public AccountAccessDeniedException(String message) {
        super(message);
    }

    public AccountAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountAccessDeniedException() {
        super("Account access denied.");
    }
}
