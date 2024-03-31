package com.example.demo.service.exception;

public class AuthorRegistryException extends RuntimeException {
    public AuthorRegistryException(String message) {
        super(message);
    }

    public AuthorRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
