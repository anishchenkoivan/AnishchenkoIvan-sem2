package com.example.authorregistryservice.entity;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record Author(@NotNull String firstName, @NotNull String lastName) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(firstName, author.firstName) && Objects.equals(lastName, author.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}
