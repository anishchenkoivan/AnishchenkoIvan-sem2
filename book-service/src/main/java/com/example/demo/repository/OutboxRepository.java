package com.example.demo.repository;

import com.example.demo.entity.OutboxRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {
}
