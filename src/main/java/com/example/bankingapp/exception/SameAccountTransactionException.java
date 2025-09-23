package com.example.bankingapp.exception;

public class SameAccountTransactionException extends RuntimeException {
    public SameAccountTransactionException(String message) {
        super(message);
    }

    public SameAccountTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SameAccountTransactionException() {
        super("You cannot transfer between the same accounts.");
    }
}
