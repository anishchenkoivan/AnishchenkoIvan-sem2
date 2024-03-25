package com.example.demo.service;

import com.example.demo.entity.Author;
import com.example.demo.repository.AuthorRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({AuthorService.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Rollback(false)
public class AuthorServiceTest {

    @Container
    @ServiceConnection
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13");

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorService authorService;

    @Test
    @Order(0)
    void shouldCreateAuthor() {
        authorService.createAuthor("George", "Orwell");
        int authorsAmount = authorRepository.findAll().size();
        assertEquals(1, authorsAmount);
        Author author = authorRepository.findById(1L).orElse(null);
        assertNotNull(author);
        assertEquals("George", author.getFirstName());
        assertEquals("Orwell", author.getLastName());
    }

    @Test
    @Order(1)
    void ShouldUpdateAuthorName() {
        int authorsAmount = authorRepository.findAll().size();
        assertEquals(1, authorsAmount);
        authorService.updateAuthorName(1L, "Alexandr", "Pushkin");
        Author author = authorRepository.findById(1L).orElse(null);
        assertNotNull(author);
        assertEquals("Alexandr", author.getFirstName());
        assertEquals("Pushkin", author.getLastName());
    }

    @Test
    @Order(2)
    void shouldDeleteAuthor() {
        authorService.deleteAuthor(1L);
        int authorsAmount = authorRepository.findAll().size();
        assertEquals(0, authorsAmount);
    }
}
