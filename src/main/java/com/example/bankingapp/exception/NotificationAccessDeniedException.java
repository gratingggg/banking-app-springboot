package com.example.bankingapp.exception;

public class NotificationAccessDeniedException extends RuntimeException {
    public NotificationAccessDeniedException(String message) {
        super(message);
    }

    public NotificationAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationAccessDeniedException() {
        super("You are trying to view someone else's notifications");
    }
}
