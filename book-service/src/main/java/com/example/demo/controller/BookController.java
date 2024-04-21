package com.example.demo.controller;

import com.example.demo.controller.request.BookCreateRequest;
import com.example.demo.controller.request.BookUpdateRequest;
import com.example.demo.controller.response.ApiError;
import com.example.demo.entity.Tag;
import com.example.demo.service.AuthorService;
import com.example.demo.service.BookRatingCheckService;
import com.example.demo.service.BookService;
import com.example.demo.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/books")
@Validated
public class BookController {
    private final BookService bookService;
    private final AuthorService authorService;
    private final TagService tagService;
    private final BookRatingCheckService bookRatingCheckService;

    @Autowired
    public BookController(BookService bookService, AuthorService authorService, TagService tagService, BookRatingCheckService bookRatingCheckService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.tagService = tagService;
        this.bookRatingCheckService = bookRatingCheckService;
    }

    @PostMapping()
    public void createBook(@NotNull @RequestBody @Valid BookCreateRequest request) {
        Set<Tag> tags = new HashSet<>();
        for (String name : request.tags()) {
            tags.add(tagService.getTagByName(name));
        }
        authorService.createBook(request.authorId(), request.title(), tags);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@NotNull @PathVariable Long id) {
        authorService.deleteBook(id);
    }

    @PutMapping("/{id}")
    public void updateBook(@NotNull @PathVariable Long id, @NotNull @RequestBody @Valid BookUpdateRequest request) {
        bookService.updateBookTitle(id, request.title());
    }

    @PutMapping("/{id}/rating")
    public void getRating(@NotNull @PathVariable Long id) {
        bookRatingCheckService.calculateRating(id);
    }

    public void buy(@NotNull @PathVariable Long id) {

    }

    @ExceptionHandler
    public ResponseEntity<ApiError> noSuchElementExceptionHandler(NoSuchElementException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}