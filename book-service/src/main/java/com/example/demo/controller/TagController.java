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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/tags")
@Validated
public class TagController {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping()
    public void createTag(@NotNull @RequestBody @Valid TagCreateRequest request) {
        tagService.createTag(request.name());
    }

    @DeleteMapping("/{id}")
    public void deleteTag(@NotNull @PathVariable Long id) {
        tagService.deleteTag(id);
    }

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
