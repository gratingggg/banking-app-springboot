package com.example.bankingapp.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause){
        super(message, cause);
    }

    public UsernameAlreadyExistsException(){
        super("The username is already taken. Please choose another username.");
    }
}
