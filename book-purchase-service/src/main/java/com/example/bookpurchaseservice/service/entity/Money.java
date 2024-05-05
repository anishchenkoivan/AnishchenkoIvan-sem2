package com.example.bookpurchaseservice.service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "money")
public class Money {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    protected Money() {}
    public Money(int amount) {}

    public int getAmount() {
        return amount;
    }

    public void decreaseAmount(int delta) {
        this.amount -= delta;
    }
}
