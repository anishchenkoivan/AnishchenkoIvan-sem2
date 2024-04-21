package com.example.demo.service;

import com.example.demo.entity.OutboxRecord;
import com.example.demo.repository.OutboxRepository;
import com.example.demo.service.exception.OrderPaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OutboxScheduler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    public final OutboxRepository outboxRepository;

    public OutboxScheduler(KafkaTemplate<String, String> kafkaTemplate, @Value("${payment-request-topic}") String topic, OutboxRepository outboxRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Scheduled(fixedDelay = 3000)
    public void processOutbox() {
        List<OutboxRecord> result = outboxRepository.findAll();
        for (OutboxRecord outboxRecord : result) {
            CompletableFuture<SendResult<String, String>> sendResult = kafkaTemplate.send(topic, outboxRecord.getData());
            try {
                sendResult.get(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Unexpected thread interruption", e);
            } catch (ExecutionException e) {
                throw new OrderPaymentException("Couldn't send message to kafka", e);
            } catch (TimeoutException e) {
                throw new OrderPaymentException("Couldn't send message to kafka due to timeout", e);
            }
        }
        outboxRepository.deleteAll(result);
    }
}
