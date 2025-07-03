package com.kifiya.paymentprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.kifiya.paymentprocessing.domain", "com.kifiya.paymentprocessing.outbox"})
@EnableJpaRepositories(basePackages = "com.kifiya.paymentprocessing.repository")
public class PaymentprocessingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentprocessingApplication.class, args);
    }
}
