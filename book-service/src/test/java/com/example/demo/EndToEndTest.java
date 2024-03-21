package com.example.demo;

import com.example.demo.controller.request.*;
import com.example.demo.entity.Author;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.response.EmtyResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @Test
    @Order(0)
    void ShouldCreateAuthor() {
        ResponseEntity<EmtyResponse> createAuthorResponse = rest.postForEntity("/api/authors", new AuthorCreateRequest("William", "Shakespeare"), EmtyResponse.class);
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
        ResponseEntity<EmtyResponse> createBookResponse = rest.postForEntity("/api/books", new BookCreateRequest(1L, "Old man and the sea", Collections.emptySet()), EmtyResponse.class);
        assertTrue(createBookResponse.getStatusCode().is2xxSuccessful());
        int booksAmount = bookRepository.findAll().size();
        assertEquals(1, booksAmount);
    }

    @Test
    @Order(3)
    void shouldCreateTag() {
        ResponseEntity<EmtyResponse> createTagResponse = rest.postForEntity("/api/tags", new TagCreateRequest("Classic"), EmtyResponse.class);
        assertTrue(createTagResponse.getStatusCode().is2xxSuccessful());
    }
}
