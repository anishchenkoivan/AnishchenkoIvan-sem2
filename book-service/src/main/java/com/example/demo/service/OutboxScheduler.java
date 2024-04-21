package com.example.demo.service;

import com.example.demo.entity.OutboxRecord;
import com.example.demo.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    public void processOutbox() {
        List<OutboxRecord> result = outboxRepository.findAll();
        for (OutboxRecord outboxRecord : result) {
            CompletableFuture<SendResult<String, String>> sendResult = kafkaTemplate.send(topic, outboxRecord.getData());
            
        }
    }
}
