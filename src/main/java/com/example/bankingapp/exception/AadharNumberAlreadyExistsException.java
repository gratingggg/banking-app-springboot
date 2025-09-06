package com.example.bankingapp.exception;

public class AadharNumberAlreadyExistsException extends RuntimeException {
    public AadharNumberAlreadyExistsException(String message) {
        super(message);
    }

    public AadharNumberAlreadyExistsException(String message, Throwable cause){
        super(message, cause);
    }

    public AadharNumberAlreadyExistsException(){
        super("The aadhar number you entered is already registered. Please enter another aadhar number.");
    }
}
