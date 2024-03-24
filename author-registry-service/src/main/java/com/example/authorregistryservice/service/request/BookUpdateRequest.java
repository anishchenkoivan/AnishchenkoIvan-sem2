package com.example.authorregistryservice.service.request;

import jakarta.validation.constraints.NotNull;

public record BookUpdateRequest(@NotNull String authorFirstName, @NotNull String authorLastName, @NotNull String bookOldTitle, @NotNull String bookNewTitle) {
}
