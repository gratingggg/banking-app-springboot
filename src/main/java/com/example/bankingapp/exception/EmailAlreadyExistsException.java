package com.example.bankingapp.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause){
        super(message, cause);
    }

    public EmailAlreadyExistsException(){
        super("The email you entered is already registered. Please enter another email.");
    }
}
