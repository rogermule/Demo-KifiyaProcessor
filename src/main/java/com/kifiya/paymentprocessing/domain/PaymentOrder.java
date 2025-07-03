package com.kifiya.paymentprocessing.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class PaymentOrder {
    @Id
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    
    private BigDecimal amount;
    
    private String currency;
    
    private String clientReference;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}