package com.example.demo.controller.request;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BookUpdateRequest(@NotNull String author, @NotNull String title, @NotNull Set<String> tags) {
}
