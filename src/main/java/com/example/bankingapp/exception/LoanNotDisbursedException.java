package com.example.bankingapp.exception;

public class LoanNotDisbursedException extends RuntimeException{
    public LoanNotDisbursedException() {
        super("Loan not disbursed yet.");
    }

    public LoanNotDisbursedException(String message) {
        super(message);
    }

    public LoanNotDisbursedException(String message, Throwable cause) {
        super(message, cause);
    }
}
