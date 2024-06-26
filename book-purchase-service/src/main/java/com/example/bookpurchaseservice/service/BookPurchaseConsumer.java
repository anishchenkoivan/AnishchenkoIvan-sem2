package com.example.bookpurchaseservice.service;

import com.example.bookpurchaseservice.repository.MoneyRepository;
import com.example.bookpurchaseservice.service.entity.Money;
import com.example.bookpurchaseservice.service.exception.PurchaseException;
import com.example.bookpurchaseservice.service.request.BookPaymentRequest;
import com.example.bookpurchaseservice.service.response.BookPaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class BookPurchaseConsumer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookPurchaseConsumer.class);
    private final String responseTopic;
    private final MoneyRepository moneyRepository;
    private final int BOOK_PRICE = 100;

    public BookPurchaseConsumer(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, @Value("${payment-response-topic}") String responseTopic, MoneyRepository moneyRepository) {
        this.objectMapper = objectMapper;;
        this.kafkaTemplate = kafkaTemplate;
        this.responseTopic = responseTopic;
        this.moneyRepository = moneyRepository;
    }

    @KafkaListener(topics = {"${payment-request-topic}"})
    @Transactional(propagation = Propagation.REQUIRED)
    public void makePurchase(String message, Acknowledgment acknowledgment) {
        try {
            BookPaymentRequest parsedMessage = objectMapper.readValue(message, BookPaymentRequest.class);

            Money money = moneyRepository.getForUpdate().orElse(new Money(0));
            if (money.getAmount() >= BOOK_PRICE) {
                money.decreaseAmount(BOOK_PRICE);
                sendResponse(parsedMessage.bookId(), true);
            } else {
                sendResponse(parsedMessage.bookId(), false);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse message {}", e.getMessage());
        } finally {
            acknowledgment.acknowledge();
        }
    }

    public void sendResponse(Long bookId, boolean success) {
        try {
            String message = objectMapper.writeValueAsString(new BookPaymentResponse(bookId, success));
            CompletableFuture<SendResult<String, String>> sendResult = kafkaTemplate.send(responseTopic, message);
            sendResult.get(2, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to send rating for book with id = {}", bookId);
        } catch (ExecutionException e) {
            LOGGER.error("Couldn't send message to kafka", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Unexpected thread interruption", e);
        } catch (TimeoutException e) {
            LOGGER.error("Couldn't send message to kafka due to timeout", e);
        }
    }
}