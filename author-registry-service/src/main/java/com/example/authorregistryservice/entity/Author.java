package com.example.authorregistryservice.entity;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public record Author(@NotBlank String firstName, @NotBlank String lastName) {
}
