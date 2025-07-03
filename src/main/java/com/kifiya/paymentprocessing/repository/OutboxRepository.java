package com.kifiya.paymentprocessing.repository;

import com.kifiya.paymentprocessing.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByPublishedFalseAndFailedFalse();
}