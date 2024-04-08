package com.example.bookratingservice.service;

public interface MessageProcessor<M> {
    void processMessage(M message);
}
