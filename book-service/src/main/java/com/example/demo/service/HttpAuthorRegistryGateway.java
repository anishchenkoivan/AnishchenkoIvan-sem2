package com.example.demo.service;

import com.example.demo.service.exception.AuthorRegistryException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@ConditionalOnProperty(value = "author-registry-gateway.mode", havingValue = "http")
public class HttpAuthorRegistryGateway implements AuthorRegistryGateway {
    private final RestTemplate restTemplate;

    @Autowired
    public HttpAuthorRegistryGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RateLimiter(name = "checkAuthor", fallbackMethod = "fallbackRateLimiter")
    @CircuitBreaker(name = "checkAuthor", fallbackMethod = "fallbackCircuitBreaker")
    @Retry(name = "checkAuthor")
    public boolean checkAuthor(String authorFirstName, String authorLastName, String bookTitle, String requestId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-REQUEST-ID", requestId);

            ResponseEntity<Boolean> authorValidationResponse = restTemplate.exchange(
                    "/api/authors-check?firstName={firstName}&lastName={lastName}&bookTitle={bookTitle}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Boolean>() {
                    },
                    Map.of("firstName", authorFirstName, "lastName", authorLastName, "bookTitle", bookTitle)
            );
            if (authorValidationResponse.getStatusCode().is2xxSuccessful()) {
                return Objects.requireNonNullElse(authorValidationResponse.getBody(), false);
            }
            throw new AuthorRegistryException("Unexpected status code " + authorValidationResponse);
        } catch (RestClientException e) {
            throw new AuthorRegistryException("Error during requesting author registry service + " + e.getMessage(), e);
        }
    }

    public boolean fallbackRateLimiter(String authorFirstName, String authorLastName, String bookTitle, String requestId, RequestNotPermitted e) {
        throw new AuthorRegistryException(e.getMessage(), e);
    }

    public boolean fallbackCircuitBreaker(String authorFirstName, String authorLastName, String bookTitle, String requestId, CallNotPermittedException e) {
        throw new AuthorRegistryException(e.getMessage(), e);
    }
}
