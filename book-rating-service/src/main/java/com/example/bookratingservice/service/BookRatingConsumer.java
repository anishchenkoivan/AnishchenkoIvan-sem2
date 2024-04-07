package com.example.bookratingservice.service;

import com.example.bookratingservice.service.dto.AssignBookRatingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookRatingConsumer {
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookRatingConsumer.class);

    @Autowired
    public BookRatingConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"${topic-to-consume-message}"})
    public void assignBookRating(String message) {
        try {
            AssignBookRatingRequest parsedMessage = objectMapper.readValue(message, AssignBookRatingRequest.class);
            
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse message {}", message);
        }
    }

    public void sendAssignedRating(Long rating) {

    }
}
