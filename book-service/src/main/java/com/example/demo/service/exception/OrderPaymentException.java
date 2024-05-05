package com.example.demo.service.exception;

public class OrderPaymentException extends RuntimeException {
    public OrderPaymentException(String message) {
        super(message);
    }

    public OrderPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
