package com.example.bankingapp.exception;

public class TransactionAccessDeniedException extends RuntimeException {
    public TransactionAccessDeniedException(String message) {
        super(message);
    }

    public TransactionAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionAccessDeniedException() {
        super("You are trying to access someone else's transaction.");
    }
}
