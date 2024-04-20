package com.example.demo.controller.request;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BookUpdateRequest(@NotNull String title) {
}
