package com.example.bankingapp.exception;

public class LoanAccessDeniedException extends RuntimeException{
    public LoanAccessDeniedException() {
        super("Loan access denied.");
    }

    public LoanAccessDeniedException(String message) {
        super(message);
    }

    public LoanAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
