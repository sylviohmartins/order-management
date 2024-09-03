package com.example.order.management.model;

import com.example.order.management.model.nested.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private UUID id;

    private String customer;

    private List<String> items;

    private PaymentDetails paymentDetails;

}
