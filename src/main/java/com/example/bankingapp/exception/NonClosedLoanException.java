package com.example.bankingapp.exception;

public class NonClosedLoanException extends RuntimeException {
    public NonClosedLoanException(String message) {
        super(message);
    }

    public NonClosedLoanException(String message, Throwable cause){
        super(message, cause);
    }

    public NonClosedLoanException(){
        super("You currently have active loan. Please clear them before deleting the account.");
    }
}
