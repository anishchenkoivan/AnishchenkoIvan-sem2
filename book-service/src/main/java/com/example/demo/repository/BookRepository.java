package com.example.demo.repository;

import com.example.demo.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();

    Optional<Book> save(Book book);
    List<Book> findByTag(String tag);
    Optional<Book> deleteById(Long id);

    Optional<Book> findById(Long id);
}