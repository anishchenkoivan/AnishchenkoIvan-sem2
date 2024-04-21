package com.example.demo.entity;

import com.example.demo.service.event.OrderPaymentInitiatedEvent;
import com.example.demo.service.exception.OrderPaymentException;
import jakarta.persistence.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book extends AbstractAggregateRoot<Book> {
    public enum Status {
        NOT_SOLD,
        SOLD,
        PAYMENT_PENDING
    }
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

    private Integer rating;

    @Enumerated(EnumType.ORDINAL)
    private Status status = Status.NOT_SOLD;

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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void buyBook() {
        if (status.equals(Status.PAYMENT_PENDING)) {
            throw new OrderPaymentException("Order is in the processs of payment");
        }
        setStatus(Status.PAYMENT_PENDING);
        registerEvent(new OrderPaymentInitiatedEvent(id));
    }

    protected Book() {}

    public Book(long id, Author author, String title, Set<Tag> tags, Integer rating) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.tags = tags;
        this.rating = rating;
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

