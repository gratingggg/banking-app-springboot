package com.example.bankingapp.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationNotFoundException() {
        super("Notification not found.");
    }
}
