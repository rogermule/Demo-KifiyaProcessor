package com.kifiya.paymentprocessing.api;

import com.kifiya.paymentprocessing.domain.PaymentOrder;
import com.kifiya.paymentprocessing.service.PaymentService;
import com.kifiya.paymentprocessing.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentOrder> createPayment(@Valid @RequestBody PaymentOrderRequest request) {
        PaymentOrder order = new PaymentOrder();
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setAmount(request.getAmount());
        order.setCurrency(request.getCurrency());
        order.setClientReference(request.getClientReference());
        return new ResponseEntity<>(paymentService.createPayment(order), HttpStatus.CREATED);
    }

    @GetMapping("/{idempotencyKey}")
    public ResponseEntity<PaymentOrder> getPayment(@PathVariable String idempotencyKey) {
        Optional<PaymentOrder> order = paymentService.getPaymentByIdempotencyKey(idempotencyKey);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{idempotencyKey}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String idempotencyKey) {
        Optional<PaymentOrder> order = paymentService.getPaymentByIdempotencyKey(idempotencyKey);
        return order.map(o -> ResponseEntity.ok(o.getStatus().name()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}