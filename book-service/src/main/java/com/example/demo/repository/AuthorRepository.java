package com.example.demo.repository;

import com.example.demo.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b WHERE b.id = :bookId")
    Optional<Author> findByBookId(Long bookId);
}
