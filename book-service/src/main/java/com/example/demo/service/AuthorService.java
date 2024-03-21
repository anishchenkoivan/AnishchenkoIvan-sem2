package com.example.demo.service;

import com.example.demo.entity.Author;
import com.example.demo.entity.Tag;
import com.example.demo.repository.AuthorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional
    public void createAuthor(String firstName, String lastName) {
        Author author = new Author(firstName, lastName);
        authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id).orElseThrow();
        authorRepository.delete(author);
    }

    @Transactional
    public void updateAuthorName(Long id, String firstName, String lastName) {
        Author author = authorRepository.findById(id).orElseThrow();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        authorRepository.save(author);
    }

    @Transactional
    public void createBook(Long authorId, String title, Set<Tag> tags) {
        Author author = authorRepository.findById(authorId).orElseThrow();
        author.createBook(title, tags);
        authorRepository.save(author);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Author author = authorRepository.findByBookId(bookId).orElseThrow();
        author.deleteBook(bookId);
    }
}
