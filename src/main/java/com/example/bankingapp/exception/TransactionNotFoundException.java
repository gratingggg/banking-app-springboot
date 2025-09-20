package com.example.bankingapp.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionNotFoundException() {
        super("Transaction not found.");
    }
}
