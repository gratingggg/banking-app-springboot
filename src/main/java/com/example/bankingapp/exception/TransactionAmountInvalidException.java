package com.example.bankingapp.exception;

public class TransactionAmountInvalidException extends RuntimeException {
    public TransactionAmountInvalidException(String message) {
        super(message);
    }

    public TransactionAmountInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionAmountInvalidException() {
        super("Transaction amount is invalid. Please enter a positive amount.");
    }
}
