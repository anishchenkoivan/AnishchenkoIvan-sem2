package com.example.demo.controller;

import com.example.demo.controller.request.TagCreateRequest;
import com.example.demo.controller.request.TagUpdateRequest;
import com.example.demo.controller.response.ApiError;
import com.example.demo.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/tags")
@Validated
@PreAuthorize("isAuthenticated()")
public class TagController {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping()
    public void createTag(@NotNull @RequestBody @Valid TagCreateRequest request) {
        tagService.createTag(request.name());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteTag(@NotNull @PathVariable Long id) {
        tagService.deleteTag(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public void updateTag(@NotNull @PathVariable Long id, @NotNull @RequestBody @Valid TagUpdateRequest request) {
        tagService.updateTag(id, request.name());
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> noSuchElementExceptionHandler(NoSuchElementException e) {
        return new ResponseEntity<>(
                new ApiError(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
