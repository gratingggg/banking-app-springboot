package com.example.bankingapp.exception;

public class EmployeeInactiveException extends RuntimeException {
    public EmployeeInactiveException(String message) {
        super(message);
    }

    public EmployeeInactiveException(String message, Throwable cause){
        super(message, cause);
    }

    public EmployeeInactiveException(){
        super("Your status is currently not active. Please contact the admin.");
    }
}
