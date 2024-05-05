package com.example.bookpurchaseservice.service;

import com.example.bookpurchaseservice.repository.MoneyRepository;
import com.example.bookpurchaseservice.service.entity.Money;
import com.example.bookpurchaseservice.service.request.BookPaymentRequest;
import com.example.bookpurchaseservice.service.response.BookPaymentResponse;
import com.example.bookpurchaseservice.service.stabs.KafkaTestConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookPurchaseConsumerTest {
    @Container
    @ServiceConnection
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13");

    @Container
    @ServiceConnection
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private BookPurchaseConsumer bookPurchaseConsumer;

    @Autowired
    private MoneyRepository moneyRepository;

    private static KafkaTestConsumer consumer;

    @BeforeAll
    static void setUpConsumer() {
        consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "test-book-service-group");
        consumer.subscribe(List.of("test-payment-response"));
    }

    @Test
    @Order(0)
    void shouldSuccessfullyMakePurchase() throws JsonProcessingException, InterruptedException {
        String requestData = objectMapper.writeValueAsString(new BookPaymentRequest(1L));

        kafkaTemplate.send("test-payment-request", requestData);

        ConsumerRecords<String, String> records = consumer.poll();
        assertEquals(0, records.count());
    }

    @Test
    @Transactional
    @Order(1)
    void shouldMakePurchaseAndWithdrawMoney() throws JsonProcessingException, InterruptedException {
        String requestData = objectMapper.writeValueAsString(new BookPaymentRequest(1L));
        assertDoesNotThrow(() -> bookPurchaseConsumer.makePurchase(requestData, new Acknowledgment() {
            @Override
            public void acknowledge() {
            }
        }));

        ConsumerRecords<String, String> records = consumer.poll();

        assertEquals(1, records.count());
        Money money = moneyRepository.getForUpdate().orElseThrow();
        assertEquals(400, money.getAmount());
    }

    @Test
    @Transactional
    @Order(2)
    void shouldCancelPurchaseDueToLowBalance() throws JsonProcessingException, InterruptedException {
        for (int i = 0; i < 5; i++) {
            String requestData = objectMapper.writeValueAsString(new BookPaymentRequest(3131L));
            bookPurchaseConsumer.makePurchase(requestData, new Acknowledgment() {
                @Override
                public void acknowledge() {
                }
            });
        }
        Thread.sleep(1000);
        ConsumerRecords<String, String> records = consumer.poll();
        consumer.poll();

        Money money = moneyRepository.getForUpdate().orElseThrow();
        assertEquals(0, money.getAmount());

        String requestData = objectMapper.writeValueAsString(new BookPaymentRequest(1L));
        bookPurchaseConsumer.makePurchase(requestData, new Acknowledgment() {
            @Override
            public void acknowledge() {
            }
        });

        records = consumer.poll();
        assertEquals(1, records.count());
        String recordsMessage = "";

        for (ConsumerRecord<String, String> record : records) {
            recordsMessage = record.value();
        }

        BookPaymentResponse response = objectMapper.readValue(recordsMessage, BookPaymentResponse.class);
        assertFalse(response.success());
    }
}
