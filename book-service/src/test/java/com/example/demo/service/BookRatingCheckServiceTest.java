package com.example.demo.service;

import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.service.request.AssignBookRatingRequest;
import com.example.demo.service.stabs.KafkaTestConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {BookRatingCheckService.class},
        properties = {"topic-to-send-message=test-book-rating-check-request",
                "spring.kafka.consumer.group-id=test-book-service-group",
                "spring.kafka.consumer.enable-auto-commit=true"}
)
@Import({KafkaAutoConfiguration.class, BookRatingCheckServiceTest.ObjectMapperTestConfig.class})
@Testcontainers
public class BookRatingCheckServiceTest {
    @TestConfiguration
    static class ObjectMapperTestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Container
    @ServiceConnection
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookRatingCheckService bookRatingCheckService;
    @MockBean
    private BookService bookService;

    @Test
    void shouldSendMessageToKafkaSuccessfully() {
        when(bookService.getBook(1L)).thenReturn(new Book(1L, new Author("Dan", "Brown"), "Da Vinci Code", Collections.emptySet(), null));

        assertDoesNotThrow(() -> bookRatingCheckService.calculateRating(1L));

        KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "test-book-rating-service-group");
        consumer.subscribe(List.of("test-book-rating-check-request"));

        ConsumerRecords<String, String> records = consumer.poll();
        assertEquals(1, records.count());
        records.iterator().forEachRemaining(
                record -> {
                    try {
                        AssignBookRatingRequest requestMessage = objectMapper.readValue(record.value(), AssignBookRatingRequest.class);
                        assertEquals(new AssignBookRatingRequest(1L), requestMessage);
                    } catch (JsonProcessingException e) {
                        fail();
                    }

                }
        );
    }

}
