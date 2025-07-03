package com.kifiya.paymentprocessing.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentEvent {
    private UUID paymentId;
    private String idempotencyKey;
    private PaymentStatus status;
    private LocalDateTime timestamp;
}