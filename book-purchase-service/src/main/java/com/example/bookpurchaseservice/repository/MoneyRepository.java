package com.example.bookpurchaseservice.repository;

import com.example.bookpurchaseservice.service.entity.Money;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MoneyRepository extends JpaRepository<Money, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Money m where m.id = 1")
    Optional<Money> getForUpdate();
}
