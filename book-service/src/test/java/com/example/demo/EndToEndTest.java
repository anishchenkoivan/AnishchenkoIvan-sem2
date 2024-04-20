package com.example.demo;

import com.example.demo.controller.request.*;
import com.example.demo.entity.Author;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.response.EmptyResponse;
import com.example.demo.service.AuthorRegistryGateway;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"author-registry-gateway.mode=http"}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Rollback(false)
public class EndToEndTest {
    @Container
    @ServiceConnection
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @MockBean
    private AuthorRegistryGateway authorRegistryGateway;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @Order(0)
    void ShouldCreateAuthor() {
        ResponseEntity<EmptyResponse> createAuthorResponse = rest.postForEntity("/api/authors", new AuthorCreateRequest("William", "Shakespeare"), EmptyResponse.class);
        assertTrue(createAuthorResponse.getStatusCode().is2xxSuccessful());
        int authorsAmount = authorRepository.findAll().size();
        assertEquals(1, authorsAmount);
    }

    @Test
    @Order(1)
    void shouldUpdateAuthor() {
        rest.put("/api/authors/{id}", new AuthorUpdateNameRequest("Ernest", "Hemingway"), Map.of("id", 1));
        Author author = authorRepository.findById(1L).orElse(null);
        assertNotNull(author);
        assertEquals("Ernest", author.getFirstName());
        assertEquals("Hemingway", author.getLastName());
    }

    @Test
    @Order(2)
    void shouldCreateBook() {
        when(authorRegistryGateway.checkAuthor(eq("Ernest"), eq("Hemingway"), eq("Old man and the sea"), any())).thenReturn(true);
        ResponseEntity<EmptyResponse> createBookResponse = rest.postForEntity("/api/books", new BookCreateRequest(1L, "Old man and the sea", Collections.emptySet()), EmptyResponse.class);
        assertTrue(createBookResponse.getStatusCode().is2xxSuccessful());
        int booksAmount = bookRepository.findAll().size();
        assertEquals(1, booksAmount);
    }

    @Test
    @Order(3)
    void shouldCreateTag() {
        ResponseEntity<EmptyResponse> createTagResponse = rest.postForEntity("/api/tags", new TagCreateRequest("Classic"), EmptyResponse.class);
        assertTrue(createTagResponse.getStatusCode().is2xxSuccessful());
    }
}
