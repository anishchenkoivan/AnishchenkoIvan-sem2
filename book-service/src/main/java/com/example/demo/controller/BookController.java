package com.example.demo.controller;

import com.example.demo.controller.request.BookCreateRequest;
import com.example.demo.controller.request.BookUpdateRequest;
import com.example.demo.controller.response.ApiError;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/books")
@Validated
public class BookController {
    private final BookRepository bookRepository;

    @Autowired
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable("id") Long id) {
        return bookRepository.findById(id).orElseThrow();
    }

    @GetMapping("/with-tag/{tag}")
    public List<Book> getBooksByTag(@PathVariable String tag) {
        return bookRepository.findByTagName(tag);
    }

    @PostMapping()
    public Book createBook(@NotNull @RequestBody @Valid BookCreateRequest request) {
        Book book = new Book(request.author(), request.title(), request.tags());
        return bookRepository.save(book);
    }

    @DeleteMapping("/{id}")
    public Book deleteBook(@NotNull @PathVariable Long id) {
        bookRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public void updateBook(@NotNull @PathVariable Long id, @NotNull @RequestBody @Valid BookUpdateRequest request) {
        bookRepository.save(new Book(id, request.author(), request.title(), request.tags()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> noSuchElementExceptionHandler(NoSuchElementException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}