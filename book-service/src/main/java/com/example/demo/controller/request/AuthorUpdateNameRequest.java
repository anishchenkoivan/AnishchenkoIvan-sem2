package com.example.demo.controller.request;

import jakarta.validation.constraints.NotNull;

public record AuthorUpdateNameRequest(@NotNull String firstName, @NotNull String lastName) {
}
