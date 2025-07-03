package com.kifiya.paymentprocessing.provider;

import com.kifiya.paymentprocessing.domain.PaymentOrder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MockPaymentProvider implements PaymentProvider {
    
    private final Random random = new Random();
    
    @Override
    public boolean processPayment(PaymentOrder order) {
        // Simulate transient failures 
        // I'm giving it 30% chance - forr testing demo
        if (random.nextDouble() < 0.3) {
            throw new RuntimeException("Transient provider failure");
        }
        
        try {
            Thread.sleep(random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // here also simulate success/failure with 80% success ratte
        return random.nextDouble() < 0.8;
    }
}