package com.example.demo.service;

import com.example.demo.service.exception.AuthorRegistryException;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {
                HttpAuthorRegistryGateway.class
        },
        properties = {
                "resilience4j.ratelimiter.instances.checkAuthor.limitForPeriod=1",
                "resilience4j.ratelimiter.instances.checkAuthor.limitRefreshPeriod=1h",
                "resilience4j.ratelimiter.instances.checkAuthor.timeoutDuration=0",
                "resilience4j.circuitbreaker.instances.checkAuthor.slowCallRateThreshold=1",
                "resilience4j.circuitbreaker.instances.checkAuthor.slowCallDurationThreshold=1000ms",
                "resilience4j.circuitbreaker.instances.checkAuthor.slidingWindowType=COUNT_BASED",
                "resilience4j.circuitbreaker.instances.checkAuthor.slidingWindowSize=1",
                "resilience4j.circuitbreaker.instances.checkAuthor.minimumNumberOfCalls=1",
                "resilience4j.circuitbreaker.instances.checkAuthor.waitDurationInOpenState=600s"
        }
)
@Import(RateLimiterAutoConfiguration.class)
@EnableAspectJAutoProxy
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthorRegistryGatewayRateLimiterTest {

    @Autowired
    AuthorRegistryGateway authorRegistryGateway;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void shouldRejectRequestAfterFirstServerSlowResponse() {
        when(restTemplate.getForEntity(
                eq("/api/authors-check?firstName={firstName}&lastName={lastName}&bookTitle={bookTitle}"),
                eq(Boolean.class),
                eq(Map.of("firstName", "Robert", "lastName", "Stevenson", "bookTitle", "Treasure Island"))
        )).thenAnswer((Answer<ResponseEntity<Boolean>>) invocation -> {
            Thread.sleep(2000);
            return new ResponseEntity<>(true, HttpStatus.OK);
        });

        when(restTemplate.exchange(
                eq("/api/authors-check?firstName={firstName}&lastName={lastName}&bookTitle={bookTitle}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Boolean>() {}),
                eq(Map.of("firstName", "Robert", "lastName", "Stevenson", "bookTitle", "Treasure Island"))
        )).thenAnswer((Answer<ResponseEntity<Boolean>>) invocation -> {
            Thread.sleep(2000);
            return new ResponseEntity<>(true, HttpStatus.OK);
        });


        assertDoesNotThrow(
                () -> authorRegistryGateway.checkAuthor("Robert", "Stevenson", "Treasure Island", "MockId")
        );
    }

    @Test
    void shouldRejectRequestAfterFirstServerFailResponse() {
        when(restTemplate.exchange(
                eq("/api/authors-check?firstName={firstName}&lastName={lastName}&bookTitle={bookTitle}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Boolean>() {}),
                eq(Map.of("firstName", "Robert", "lastName", "Stevenson", "bookTitle", "Treasure Island"))
        )).thenThrow(new RestClientException("Unexpected error"));

        assertThrows(
                AuthorRegistryException.class,
                () -> authorRegistryGateway.checkAuthor("Robert", "Stevenson", "Treasure Island", "MockId")
        );
    }
}
