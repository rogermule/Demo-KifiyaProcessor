package com.kifiya.paymentprocessing.provider;

import com.kifiya.paymentprocessing.domain.PaymentOrder;

public interface PaymentProvider {
    boolean processPayment(PaymentOrder order);
}