package com.example.order.management.service;

import com.example.order.management.model.nested.PaymentDetails;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public boolean processPayment(final PaymentDetails paymentDetails) {
        return true;
    }

}
