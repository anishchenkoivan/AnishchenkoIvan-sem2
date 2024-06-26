package com.example.authorregistryservice.service;


import com.example.authorregistryservice.entity.Author;
import com.example.authorregistryservice.service.exception.BookModifyException;
import com.example.authorregistryservice.service.exception.BookUpdateException;
import com.example.authorregistryservice.service.request.BookModifyRequest;
import com.example.authorregistryservice.service.request.BookUpdateRequest;
import com.example.authorregistryservice.service.response.ApiError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/authors-check")
@Validated
public class AuthorCheckService {
    ConcurrentHashMap<Author, Map<String, ?>> authorsData = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Boolean> computedData = new ConcurrentHashMap<>();

    @GetMapping
    public boolean verifyAuthor(@NotNull @RequestParam String firstName, @NotNull @RequestParam String lastName, @NotNull @RequestParam String bookTitle, @NotNull @RequestHeader("X-REQUEST-ID") String requestId) {
        final Author author = new Author(firstName, lastName);
        boolean result = computedData.computeIfAbsent(
                requestId,
                k -> authorsData.getOrDefault(author, Collections.emptyMap()).containsKey(bookTitle)
        );
        return result;
    }

    public boolean verifyAuthor(@NotNull @RequestParam String firstName, @NotNull @RequestParam String lastName, @NotNull @RequestParam String bookTitle) {
        final Author author = new Author(firstName, lastName);
        return authorsData.getOrDefault(author, Collections.emptyMap()).containsKey(bookTitle);
    }

    @PostMapping()
    public void addBook(@NotNull @RequestBody @Valid BookModifyRequest request) {
        final Author author = new Author(request.authorFirstName(), request.authorLastName());
        if (verifyAuthor(author.firstName(), author.lastName(), request.bookTitle())) {
            throw new BookModifyException("Author %s %s already has a book with title %s".formatted(author.firstName(), author.lastName(), request.bookTitle()));
        }
        authorsData.putIfAbsent(author, new ConcurrentHashMap<>());
        authorsData.get(author).put(request.bookTitle(), null);
    }

    @DeleteMapping
    public void deleteBook(@NotNull @RequestBody @Valid BookModifyRequest request) {
        final Author author = new Author(request.authorFirstName(), request.authorLastName());
        if (!verifyAuthor(author.firstName(), author.lastName(), request.bookTitle())) {
            throw new BookModifyException("Author %s %s does not have a book with title %s".formatted(author.firstName(), author.lastName(), request.bookTitle()));
        }
        authorsData.get(author).remove(request.bookTitle());
    }

    @PutMapping
    public void updateBook(@NotNull @RequestBody @Valid BookUpdateRequest request) {
        final Author author = new Author(request.authorFirstName(), request.authorLastName());
        if (!verifyAuthor(author.firstName(), author.lastName(), request.bookOldTitle())) {
            throw new BookUpdateException("Author %s %s does not have a book with title %s".formatted(author.firstName(), author.lastName(), request.bookOldTitle()));
        }
        Map<String, ?> books = authorsData.get(author);
        synchronized (books) {
            books.put(request.bookNewTitle(), null);
            books.remove(request.bookOldTitle());
        }
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> BookModifyExceptionHandler(BookModifyException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> BookUpdateExceptionHandler(BookUpdateException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
