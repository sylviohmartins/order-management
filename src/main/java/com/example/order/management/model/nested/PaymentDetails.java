package com.example.order.management.model.nested;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {

    private String cardNumber;

    private String cardHolderName;

    private String expirationDate;  // Format: MM/YY

    private String cvv;

    private String paymentMethod;  // E.g., "Credit Card", "Debit Card", "PayPal"

    private String transactionId;   // Optional: Used for tracking payments in a system

}
