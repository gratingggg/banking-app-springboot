package com.example.bankingapp.exception;

public class AmountInvalidException extends RuntimeException {
    public AmountInvalidException(String message) {
        super(message);
    }

    public AmountInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmountInvalidException() {
        super("Entered amount is invalid. Please enter a positive amount.");
    }
}
