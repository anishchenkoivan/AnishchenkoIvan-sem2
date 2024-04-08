package com.example.bookratingservice.service;

import com.example.bookratingservice.service.dto.AssignBookRatingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class RandomRatingMessageProcessor implements MessageProcessor<AssignBookRatingRequest> {
    private final BookRatingConsumer bookRatingConsumer;

    @Autowired
    public RandomRatingMessageProcessor(@Lazy BookRatingConsumer bookRatingConsumer) {
        this.bookRatingConsumer = bookRatingConsumer;
    }

    @Override
    public void processMessage(AssignBookRatingRequest message) {
        bookRatingConsumer.sendAssignedRating(message.bookId(), (int) (Math.random() * 100));
    }
}
