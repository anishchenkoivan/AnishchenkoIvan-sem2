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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    @Transactional(propagation = Propagation.REQUIRED)
    public void finishTransaction(String message) {
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
        } catch (JsonProcessingException e) {
            throw new OrderPaymentException("failed to parse json", e);
        }
    }
}
