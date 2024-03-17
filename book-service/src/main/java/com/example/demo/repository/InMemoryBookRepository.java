//package com.example.demo.repository;
//
//import com.example.demo.entity.Book;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Component
//public class InMemoryBookRepository implements BookRepository {
//    private final List<Book> books;
//    private final AtomicLong idGenerator = new AtomicLong();
//
//
//    public InMemoryBookRepository() {
//        this.books = new ArrayList<>();
//    }
//
//    @Override
//    public List<Book> findAll() {
//        return List.copyOf(books);
//    }
//
//    @Override
//    public Optional<Book> save(Book book) {
//        if (book.getId() == null) {
//            book.setId(idGenerator.incrementAndGet());
//            books.add(book);
//            return Optional.of(new Book(book));
//        } else {
//            synchronized (this) {
//                for (Book existingBook : books) {
//                    if (existingBook.getId().equals(book.getId())) {
//                        existingBook.setAuthor(book.getAuthor());
//                        existingBook.setTitle(book.getTitle());
//                        existingBook.setTags(book.getTags());
//                        return Optional.of(existingBook);
//                    }
//                }
//                return Optional.empty();
//            }
//        }
//    }
//
//    @Override
//    public List<Book> findByTag(String tag) {
//        List<Book> foundBooks = new ArrayList<>();
//        for (Book book : books) {
//            if (book.getTags().contains(tag)) {
//                foundBooks.add(book);
//            }
//        }
//        return foundBooks;
//    }
//
//    @Override
//    public synchronized Optional<Book> deleteById(Long id) {
//        Book foundBook = books.stream().filter((Book b) -> b.getId().equals(id)).findFirst().orElse(null);
//        books.removeIf((Book b) -> b.getId().equals(id));
//        if (foundBook != null) {
//            return Optional.of(foundBook);
//        }
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<Book> findById(Long id) {
//        return books.stream().filter(b -> b.getId().equals(id)).findFirst();
//    }
//}
