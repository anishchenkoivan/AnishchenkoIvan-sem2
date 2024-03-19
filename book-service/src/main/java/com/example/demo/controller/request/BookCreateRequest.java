package com.example.demo.controller.request;

import jakarta.validation.constraints.NotNull;
import com.example.demo.entity.Tag;

import java.util.Set;

public record BookCreateRequest(@NotNull Long authorId, @NotNull String title, @NotNull Set<String> tags) {
}
