package com.example.bookratingservice.service;

import com.example.bookratingservice.service.dto.AssignBookRatingRequest;
import com.example.bookratingservice.service.dto.AssignBookRatingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class BookRatingConsumer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookRatingConsumer.class);
    private final String responseTopic;

    public BookRatingConsumer(ObjectMapper objectMapper, KafkaTemplate<Long, String> kafkaTemplate, @Value("${topic-to-send-message}") String responseTopic) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.responseTopic = responseTopic;
    }

    @KafkaListener(topics = {"${topic-to-consume-message}"})
    public void assignBookRating(String message) {
        try {
            AssignBookRatingRequest parsedMessage = objectMapper.readValue(message, AssignBookRatingRequest.class);
            sendAssignedRating(parsedMessage.bookId(), (int) (Math.random() * 100));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse message {}", message);
        }
    }

    public void sendAssignedRating(Long bookId, int rating) {
        try {
            String message = objectMapper.writeValueAsString(new AssignBookRatingResponse(bookId, rating));
            CompletableFuture<SendResult<Long, String>> sendResult = kafkaTemplate.send(responseTopic, bookId, message);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to send rating for book with id = {}", bookId);
        }
    }
}
