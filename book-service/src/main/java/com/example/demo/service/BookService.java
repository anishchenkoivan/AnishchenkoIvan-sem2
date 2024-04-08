package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.Set;

@Service
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Book getBook(long id) {
        return bookRepository.findById(id).orElseThrow();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateBookTitle(Long id, String title) {
        Book book = bookRepository.findById(id).orElseThrow();
        book.setTitle(title);
        bookRepository.save(book);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateBookRating(Long bookId, int rating) {
        Book book = getBook(bookId);
        book.setRating(rating);
        bookRepository.save(book);
    }
}
