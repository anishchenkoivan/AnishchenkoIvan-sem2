package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.OutboxRecord;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.OutboxRepository;
import com.example.demo.service.exception.OrderPaymentException;
import com.example.demo.service.request.BookPaymentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.Set;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public BookService(BookRepository bookRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.bookRepository = bookRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
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

    @Transactional(propagation = Propagation.REQUIRED)
    public void buyBook(Long bookId) {
        Book book = bookRepository.findByIdForUpdate(bookId).orElseThrow();
        book.buyBook();
        try {
            outboxRepository.save(new OutboxRecord(objectMapper.writeValueAsString(new BookPaymentRequest(bookId))));
        } catch (JsonProcessingException e) {
            throw new OrderPaymentException("failed to serialize outbox record");
        }
        bookRepository.save(book);
    }
}
