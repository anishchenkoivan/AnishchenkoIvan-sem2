package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import com.example.demo.service.exception.OrderPaymentException;
import com.example.demo.service.response.BookPaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

public class BookPurchaseConsumer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookPurchaseConsumer.class);
    private final BookRepository bookRepository;

    public BookPurchaseConsumer(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, BookRepository bookRepository) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.bookRepository = bookRepository;
    }

    @KafkaListener(topics = {"${payment-response-topic}"})
    public void finishTransaction(String message, Acknowledgment acknowledgment) {
        System.out.println("\n\n\n\n\n\n\n\n\n\n\nCATCHING THE TRANSACTION\n\n\n\n\n\n\n\n\n\n\n\n");
        try {
            BookPaymentResponse parsedMessage = objectMapper.readValue(message, BookPaymentResponse.class);
            Book book = bookRepository.findByIdForUpdate(parsedMessage.bookId()).orElseThrow();
            if (parsedMessage.success()) {
                book.setStatus(Book.Status.SOLD);
            } else {
                book.setStatus(Book.Status.ERROR);
                LOGGER.warn("Book purchase failed for book with id = {}", parsedMessage.bookId());
            }
            bookRepository.save(book);
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            throw new OrderPaymentException("failed to parse json", e);
        }
    }
}
