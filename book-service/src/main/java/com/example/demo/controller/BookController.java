package com.example.demo.controller;

import com.example.demo.controller.request.BookCreateRequest;
import com.example.demo.controller.request.BookUpdateRequest;
import com.example.demo.controller.response.ApiError;
import com.example.demo.service.AuthorService;
import com.example.demo.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/books")
@Validated
public class BookController {
    private final BookService bookService;
    private final AuthorService authorService;

    @Autowired
    public BookController(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
    }

    @PostMapping()
    public void createBook(@NotNull @RequestBody @Valid BookCreateRequest request) {
        authorService.createBook(request.authorId(), request.title(), request.tags());
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@NotNull @PathVariable Long id) {
        authorService.deleteBook(id);
    }

    @PutMapping("/{id}")
    public void updateBook(@NotNull @PathVariable Long id, @NotNull @RequestBody @Valid BookUpdateRequest request) {
        bookService.updateBookTitle(id, request.title());
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> noSuchElementExceptionHandler(NoSuchElementException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}