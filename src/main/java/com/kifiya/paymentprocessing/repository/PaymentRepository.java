package com.kifiya.paymentprocessing.repository;

import com.kifiya.paymentprocessing.domain.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentOrder, java.util.UUID> {
    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);
}