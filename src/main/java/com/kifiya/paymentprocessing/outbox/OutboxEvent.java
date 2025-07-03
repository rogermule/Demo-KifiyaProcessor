package com.kifiya.paymentprocessing.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class OutboxEvent {
    @Id
    private UUID id;
    
    private String eventType;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    private LocalDateTime createdAt;
    
    private boolean published;
    
    private boolean failed;
}