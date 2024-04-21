package com.example.bookpurchaseservice.service;

import com.example.bookpurchaseservice.repository.MoneyRepository;
import com.example.bookpurchaseservice.service.entity.Money;
import com.example.bookpurchaseservice.service.request.BookPaymentRequest;
import com.example.bookpurchaseservice.service.stabs.KafkaTestConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
public class BookPurchaseConsumerTest {
    @Container
    @ServiceConnection
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @MockBean
    private MoneyRepository moneyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldSuccessfullyMakePurchase() throws JsonProcessingException, InterruptedException {
        when(moneyRepository.getForUpdate()).thenReturn(Optional.of(new Money(500)));
        String requestData = objectMapper.writeValueAsString(new BookPaymentRequest(1L));
        KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "test-book-purchase-group");
        consumer.subscribe(List.of("test-payment-response"));

        kafkaTemplate.send("test-payment-request", requestData);

        ConsumerRecords<String, String> records = consumer.poll();
        assertEquals(1, records.count());
    }
}
