package com.urutte.exception;

public class ThreadAccessDeniedException extends RuntimeException {
    
    public ThreadAccessDeniedException(String message) {
        super(message);
    }
    
    public ThreadAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
