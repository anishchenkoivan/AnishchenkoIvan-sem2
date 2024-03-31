package com.example.demo.service;

import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.entity.Tag;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.service.exception.AuthorRegistryException;
import com.example.demo.service.exception.BookCreateException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.Set;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorRegistryGateway authorRegistryGateway;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, AuthorRegistryGateway authorRegistryGateway) {
        this.authorRepository = authorRepository;
        this.authorRegistryGateway = authorRegistryGateway;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void createAuthor(String firstName, String lastName) {
        Author author = new Author(firstName, lastName);
        authorRepository.save(author);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id).orElseThrow();
        authorRepository.delete(author);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateAuthorName(Long id, String firstName, String lastName) {
        Author author = authorRepository.findById(id).orElseThrow();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        authorRepository.save(author);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void createBook(Long authorId, String title, Set<Tag> tags) {
        Author author = authorRepository.findById(authorId).orElseThrow();
        if (authorRegistryGateway.checkAuthor(author.getFirstName(), author.getLastName(), title)) {
            author.createBook(title, tags);
            authorRepository.save(author);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteBook(Long bookId) {
        Author author = authorRepository.findByBookId(bookId).orElseThrow();
        author.deleteBook(bookId);
    }
}
