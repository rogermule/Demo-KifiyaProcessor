package com.kifiya.paymentprocessing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kifiya.paymentprocessing.domain.PaymentEvent;
import com.kifiya.paymentprocessing.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);
    
    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String EVENT_EXCHANGE = "payment.events";
    private static final String ROUTING_KEY = "payment.processed";

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void processOutbox() {
        outboxRepository.findByPublishedFalseAndFailedFalse().forEach(event -> {
            try {
                PaymentEvent paymentEvent = objectMapper.readValue(event.getPayload(), PaymentEvent.class);
                rabbitTemplate.convertAndSend(EVENT_EXCHANGE, ROUTING_KEY, event.getPayload());
                event.setPublished(true);
                outboxRepository.save(event);
            } catch (JsonProcessingException e) {
                logger.error("Failed to process outbox event with ID {}: {}", event.getId(), e.getMessage());
                event.setFailed(true);
                outboxRepository.save(event);
            }
        });
    }
}