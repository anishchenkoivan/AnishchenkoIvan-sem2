package com.example.demo.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;


    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "book_tag",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Author getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    protected Book() {}

    public Book(long id, Author author, String title, Set<Tag> tags) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.tags = tags;
    }

    public Book(Author author, String title, Set<Tag> tags) {
        this.author = author;
        this.title = title;
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book book)) {
            return false;
        }
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return Book.class.hashCode();
    }
}

