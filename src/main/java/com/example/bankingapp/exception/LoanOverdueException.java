package com.example.bankingapp.exception;

public class LoanOverdueException extends RuntimeException{
    public LoanOverdueException() {
        super("You have an overdue loan.");
    }

    public LoanOverdueException(String message) {
        super(message);
    }

    public LoanOverdueException(String message, Throwable cause) {
        super(message, cause);
    }
}
