package com.example.demo.service;

public interface AuthorRegistryGateway {
    boolean checkAuthor(String authorFirstName, String authorLastName, String bookTitle, String requestId);
}
