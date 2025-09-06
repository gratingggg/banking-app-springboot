package com.example.bankingapp.exception;

public class EmployeeInactiveException extends RuntimeException {
    public EmployeeInactiveException(String message) {
        super(message);
    }

    public EmployeeInactiveException(String message, Throwable cause){
        super(message, cause);
    }

    public EmployeeInactiveException(){
        super("Your account is invalid. Please contact the admin.");
    }
}
