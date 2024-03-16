package com.example.demo.response;

import java.util.Set;

public record BookResponse(long id, String author, String title, Set<String> tags) {
}
