package com.kifiya.paymentprocessing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kifiya.paymentprocessing.domain.PaymentEvent;
import com.kifiya.paymentprocessing.domain.PaymentOrder;
import com.kifiya.paymentprocessing.domain.PaymentStatus;
import com.kifiya.paymentprocessing.outbox.OutboxEvent;
import com.kifiya.paymentprocessing.provider.PaymentProvider;
import com.kifiya.paymentprocessing.repository.OutboxRepository;
import com.kifiya.paymentprocessing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final PaymentProvider paymentProvider;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String PAYMENT_QUEUE = "payment.queue";

    @Transactional
    public PaymentOrder createPayment(PaymentOrder order) {
        // Check for duplicates
        Optional<PaymentOrder> existing = paymentRepository.findByIdempotencyKey(order.getIdempotencyKey());
        if (existing.isPresent()) {
            PaymentOrder existingOrder = existing.get();
            if (existingOrder.getStatus() != PaymentStatus.DUPLICATE) {
                existingOrder.setStatus(PaymentStatus.DUPLICATE);
                paymentRepository.save(existingOrder);
            }
            return existingOrder;
        }

        // new payment init
        order.setId(UUID.randomUUID());
        order.setStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        PaymentOrder saved = paymentRepository.save(order);
        
        // publish to RabbitMQ queue
        try {
            rabbitTemplate.convertAndSend(PAYMENT_QUEUE, objectMapper.writeValueAsString(saved));
        } catch (JsonProcessingException e) {
            logger.error("Failed to publish payment to queue: {}", e.getMessage());
            throw new RuntimeException("Failed to publish payment to queue", e);
        }
        
        return saved;
    }

    public Optional<PaymentOrder> getPaymentByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Transactional
    public void processPayment(PaymentOrder order) {
        order.setStatus(PaymentStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(order);

        try {
            // retry logic with exponential backoff
            int maxAttempts = 3;
            long initialDelayMs = 1000;
            
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                try {
                    boolean success = paymentProvider.processPayment(order);
                    order.setStatus(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
                    break;
                } catch (Exception e) {
                    if (attempt == maxAttempts - 1) {
                        order.setStatus(PaymentStatus.FAILED);
                        break;
                    }
                    Thread.sleep(initialDelayMs * (long) Math.pow(2, attempt));
                }
            }

            order.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(order);

            // Publish event to Outbox
            PaymentEvent event = new PaymentEvent();
            event.setPaymentId(order.getId());
            event.setIdempotencyKey(order.getIdempotencyKey());
            event.setStatus(order.getStatus());
            event.setTimestamp(LocalDateTime.now());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setId(UUID.randomUUID());
            outboxEvent.setEventType("PaymentProcessed");
            try {
                outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize payment event: {}", e.getMessage());
                throw new RuntimeException("Failed to serialize payment event", e);
            }
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEvent.setPublished(false);
            outboxEvent.setFailed(false);
            
            outboxRepository.save(outboxEvent);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(order);
        }
    }
}