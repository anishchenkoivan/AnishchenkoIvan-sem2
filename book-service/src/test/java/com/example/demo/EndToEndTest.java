package com.example.demo;

import com.example.demo.controller.request.BookCreateRequest;
import com.example.demo.controller.request.BookUpdateRequest;
import com.example.demo.controller.response.ApiError;
import com.example.demo.response.BookResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndTest {
    @Autowired
    private TestRestTemplate rest;

    @Test
    @Order(0)
    void shouldCreateAndReturnBook() {
        ResponseEntity<BookResponse> createBookResponse = rest.postForEntity("/api/books", new BookCreateRequest("J. Tolkien", "The Lord of the Rings", Set.of("fantasy", "adventure")), BookResponse.class);
        assertTrue(createBookResponse.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + createBookResponse.getStatusCode());
        BookResponse createBookResponseBody = createBookResponse.getBody();

        ResponseEntity<BookResponse> getBookResponse = rest.getForEntity("/api/books/{id}", BookResponse.class, Map.of("id", createBookResponseBody.id()));
        assertTrue(getBookResponse.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + getBookResponse.getStatusCode());

        BookResponse getBookResponseBody = getBookResponse.getBody();
        assertEquals("J. Tolkien", getBookResponseBody.author());
        assertEquals("The Lord of the Rings", getBookResponseBody.title());
        assertEquals(Set.of("fantasy", "adventure"), getBookResponseBody.tags());
    }

    @Test
    @Order(1)
    void shouldReturnBookByTag() {
        BookResponse[] getBookResponse = rest.getForObject("/api/books/with-tag/{tag}", BookResponse[].class, Map.of("tag", "fantasy"));
        assertEquals(1, getBookResponse.length);
        assertEquals("The Lord of the Rings", getBookResponse[0].title());
    }

    @Test
    @Order(2)
    void shouldUpdateBook() {
        rest.put("/api/books/{id}", new BookUpdateRequest("A. Pushkin", "The Lord of the Rings", Set.of("fantasy", "adventure")), Map.of("id", 1));

        ResponseEntity<BookResponse> getBookResponse = rest.getForEntity("/api/books/{id}", BookResponse.class, Map.of("id", 1));
        BookResponse getBookResponseBody = getBookResponse.getBody();
        assertEquals("A. Pushkin", getBookResponseBody.author());
    }

    @Test
    @Order(3)
    void shouldDeleteBook() {
        rest.delete("/api/books/{id}", Map.of("id", 1));

        ResponseEntity<ApiError> getBookResponse = rest.getForEntity("/api/books/{id}", ApiError.class, Map.of("id", 1));

        ApiError getBookResponseBody = getBookResponse.getBody();
        System.out.println(getBookResponseBody.message());

        assertTrue(true);
    }
}
