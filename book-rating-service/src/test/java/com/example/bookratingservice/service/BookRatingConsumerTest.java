package com.example.bookratingservice.service;

import com.example.bookratingservice.service.dto.AssignBookRatingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@SpringBootTest(
        classes = {BookRatingConsumer.class},
        properties = {
                "spring.kafka.consumer.group-id=test-book-rating-service-group",
                "topic-to-consume-message=test-book-rating-check-request",
                "topic-to-send-message=test-book-rating-check-response",
                "spring.kafka.consumer.auto-offset-reset=earliest"
        }
)
@Import({KafkaAutoConfiguration.class, BookRatingConsumerTest.ObjectMapperTestConfig.class})
@Testcontainers
public class BookRatingConsumerTest {
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

    @MockBean
    private MessageProcessor<AssignBookRatingRequest> messageProcessor;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSendMessageToKafkaSuccessfully() throws JsonProcessingException {
        kafkaTemplate.send("test-book-rating-check-request", objectMapper.writeValueAsString(new AssignBookRatingRequest(1L)));

        await().atMost(Duration.ofSeconds(5))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() -> Mockito.verify(
                        messageProcessor, times(1)).processMessage(eq(new AssignBookRatingRequest(1L)))
                );
    }
}
