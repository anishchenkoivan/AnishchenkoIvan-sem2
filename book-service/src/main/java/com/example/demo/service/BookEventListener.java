package com.example.demo.service;

import com.example.demo.entity.OutboxRecord;
import com.example.demo.repository.OutboxRepository;
import com.example.demo.service.event.OrderPaymentInitiatedEvent;
import com.example.demo.service.exception.OrderPaymentException;
import com.example.demo.service.request.BookPaymentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;

public class BookEventListener {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public BookEventListener(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = BEFORE_COMMIT)
    public void onBookOrderInitiated(OrderPaymentInitiatedEvent event) {
        try {
            outboxRepository.save(new OutboxRecord(objectMapper.writeValueAsString(new BookPaymentRequest(event.id()))));
        } catch (JsonProcessingException e) {
            throw new OrderPaymentException("Failed to serialize order payment request", e);
        }
    }
}
