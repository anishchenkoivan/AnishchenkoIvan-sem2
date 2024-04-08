package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.service.exception.BookRatingException;
import com.example.demo.service.request.AssignBookRatingRequest;
import com.example.demo.service.response.AssignBookRatingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class BookRatingCheckService {
    private final BookService bookService;
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String sendTopic;

    public BookRatingCheckService(BookService bookService, KafkaTemplate<Long, String> kafkaTemplate, ObjectMapper objectMapper, @Value("${topic-to-send-message}") String sendTopic) {
        this.bookService = bookService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.sendTopic = sendTopic;
    }

    public void calculateRating(Long bookId) {
        Book book = bookService.getBook(bookId);
        try {
            String message = objectMapper.writeValueAsString(new AssignBookRatingRequest(bookId));
            CompletableFuture<SendResult<Long, String>> sendResult = kafkaTemplate.send(sendTopic, book.getId(), message);
        } catch (JsonProcessingException e) {
            throw new BookRatingException("Failed to create a JSON", e);
        }
    }

    @KafkaListener(topics = {"${topic-to-consume-message}"})
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveAssignedBookRating(String message) {
        try {
            AssignBookRatingResponse parsedMessage = objectMapper.readValue(message, AssignBookRatingResponse.class);
            bookService.updateBookRating(parsedMessage.bookId(), parsedMessage.rating());
        } catch (JsonProcessingException e) {
            throw new BookRatingException("Failed to read response", e);
        }
    }
}
