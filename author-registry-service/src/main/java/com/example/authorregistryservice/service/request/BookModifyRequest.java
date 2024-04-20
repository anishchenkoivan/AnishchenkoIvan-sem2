package com.example.authorregistryservice.service.request;

import jakarta.validation.constraints.NotNull;

public record BookModifyRequest(@NotNull String authorFirstName, @NotNull String authorLastName, @NotNull String bookTitle) {
}
