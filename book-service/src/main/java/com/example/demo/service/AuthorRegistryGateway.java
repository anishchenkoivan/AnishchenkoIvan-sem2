package com.example.demo.service;

import com.example.demo.service.exception.AuthorRegistryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
public class AuthorRegistryGateway {
    private final RestTemplate restTemplate;

    @Autowired
    public AuthorRegistryGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean checkAuthor(String authorFirstName, String authorLastName, String bookTitle) {
        try {
            ResponseEntity<Boolean> authorValidationResponse = restTemplate.getForEntity(
                    "/api/authors-check?firstName={firstName}&lastName={lastName}&bookTitle={bookTitle}",
                    Boolean.class,
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
}
