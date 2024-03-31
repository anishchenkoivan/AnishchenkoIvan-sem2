package com.example.demo.service.exception;

public class BookCreateException extends RuntimeException {
    public BookCreateException(String message) {
        super(message);
    }
}
