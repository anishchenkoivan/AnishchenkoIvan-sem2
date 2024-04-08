package com.example.demo.service.exception;

public class BookRatingException extends RuntimeException {
    public BookRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
