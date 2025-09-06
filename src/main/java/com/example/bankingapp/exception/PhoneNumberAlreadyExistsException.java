package com.example.bankingapp.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String message) {
        super(message);
    }

    public PhoneNumberAlreadyExistsException(String message, Throwable cause){
        super(message, cause);
    }

    public PhoneNumberAlreadyExistsException(){
        super("The phone number is already registered. Please enter another phone number.");
    }
}
