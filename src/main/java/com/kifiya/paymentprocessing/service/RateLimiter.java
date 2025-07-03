package com.kifiya.paymentprocessing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kifiya.paymentprocessing.domain.PaymentOrder;
import com.kifiya.paymentprocessing.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    private static final String PAYMENT_QUEUE = "payment.queue";

    @RabbitListener(queues = PAYMENT_QUEUE)
    public void processQueue(String message) {
        // Limit rate using 2 TPS using token bucket
        Long tokenCount = redisTemplate.opsForValue().increment("rate_limit:tokens", 1);
        if (tokenCount == null || tokenCount > 2) {
            redisTemplate.opsForValue().decrement("rate_limit:tokens");
            return;
        }

        // Process payment
        try {
            PaymentOrder order = objectMapper.readValue(message, PaymentOrder.class);
            paymentRepository.findById(order.getId())
                    .ifPresent(payment -> {
                        paymentService.processPayment(payment);
                        meterRegistry.counter("payments.processed").increment();
                    });
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize payment order: {}", e.getMessage());
        }

        // tokens eexpire after 1 second
        redisTemplate.expire("rate_limit:tokens", 1, TimeUnit.SECONDS);
    }
}