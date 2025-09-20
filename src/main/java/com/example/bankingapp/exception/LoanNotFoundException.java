package com.example.bankingapp.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanNotFoundException() {
        super("Loan not found.");
    }
}
