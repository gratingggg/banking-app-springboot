package com.example.bankingapp.exception;

public class LoanAlreadyProcessedException extends RuntimeException {
    public LoanAlreadyProcessedException(String message) {
        super(message);
    }

    public LoanAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanAlreadyProcessedException() {
        super("Loan already processed.");
    }


}
