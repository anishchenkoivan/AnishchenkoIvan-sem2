package com.example.demo.controller;

import com.example.demo.controller.request.AuthorCreateRequest;
import com.example.demo.controller.request.AuthorUpdateNameRequest;
import com.example.demo.controller.response.ApiError;
import com.example.demo.service.AuthorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/authors")
@Validated
public class AuthorController {
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping()
    public void createAuthor(@NotNull @RequestBody @Valid AuthorCreateRequest authorCreateRequest) {
        authorService.createAuthor(authorCreateRequest.firstName(), authorCreateRequest.lastName());
    }

    @DeleteMapping("/{id}")
    public void deleteAuthor(@NotNull @PathVariable Long id) {
        authorService.deleteAuthor(id);
    }

    @PutMapping("/{id}")
    public void updateAuthorName(@NotNull @PathVariable Long id, @NotNull @RequestBody @Valid AuthorUpdateNameRequest authorUpdateNameRequest) {
        authorService.updateAuthorName(id, authorUpdateNameRequest.firstName(), authorUpdateNameRequest.lastName());
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> noSuchElementExceptionHandler(NoSuchElementException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
