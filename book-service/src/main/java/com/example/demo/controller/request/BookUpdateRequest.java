package com.example.demo.controller.request;

import com.example.demo.entity.Author;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BookUpdateRequest(@NotNull Author author, @NotNull String title, @NotNull Set<String> tags) {
}
