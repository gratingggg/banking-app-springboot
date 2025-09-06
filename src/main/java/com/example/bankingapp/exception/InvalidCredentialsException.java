package com.example.bankingapp.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause){
        super(message, cause);
    }

    public InvalidCredentialsException(){
        super("Wrong credentials. Please try again.");
    }
}
