package com.example.bankingapp.exception;

public class LoanNotActiveException extends RuntimeException{
    public LoanNotActiveException() {
        super("Loan is not active currently.");
    }

    public LoanNotActiveException(String message) {
        super(message);
    }

    public LoanNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
