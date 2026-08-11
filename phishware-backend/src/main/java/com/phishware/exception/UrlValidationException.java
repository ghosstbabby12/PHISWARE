package com.phishware.exception;

public class UrlValidationException extends RuntimeException {
    public UrlValidationException(String message) {
        super(message);
    }
}
