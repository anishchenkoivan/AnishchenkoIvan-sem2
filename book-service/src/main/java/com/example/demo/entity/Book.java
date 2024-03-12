package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.jackson.JsonComponent;

import java.util.Set;

public class Book {
    private Long id;
    private String author;
    private String title;
    private Set<String> tags;

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Book(long id, String author, String title, Set<String> tags) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.tags = tags;
    }

    public Book(String author, String title, Set<String> tags) {
        this.author = author;
        this.title = title;
        this.tags = tags;
    }

    public Book(Book book) {
        this.id = book.getId();
        this.author = book.getAuthor();
        this.title = book.getTitle();
        this.tags = book.getTags();
    }
}

