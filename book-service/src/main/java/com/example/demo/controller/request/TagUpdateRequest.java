package com.example.demo.controller.request;

import jakarta.validation.constraints.NotNull;

public record TagUpdateRequest(@NotNull String name) {
}
