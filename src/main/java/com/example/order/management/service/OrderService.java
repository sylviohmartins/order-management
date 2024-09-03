package com.example.order.management.service;

import com.example.order.management.exception.PaymentException;
import com.example.order.management.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final DynamoDbClient dynamoDbClient;
    private final SqsClient sqsClient;

    @Value("${orders.table.name}")
    private String ordersTable;

    @Value("${sqs.queue.url}")
    private String sqsQueueUrl;

    public void processOrder(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");

        logger.info("Processing order ID: {}", order.getId());

        if (!paymentService.processPayment(order.getPaymentDetails())) {
            logger.error("Payment failed for order ID: {}", order.getId());
            throw new PaymentException("Payment failed for order ID: " + order.getId());
        }

        logger.info("Payment successful for order ID: {}", order.getId());

        inventoryService.updateInventory(order.getItems());

        saveOrderToDynamoDB(order);

        sendOrderToSQS(order);

        notificationService.sendOrderConfirmation(order);

        logger.info("Order ID: {} processed successfully.", order.getId());
    }

    private void saveOrderToDynamoDB(Order order) {
        try {
            Map<String, AttributeValue> itemValues = new HashMap<>();
            itemValues.put("OrderId", AttributeValue.builder().s(order.getId().toString()).build());
            itemValues.put("Customer", AttributeValue.builder().s(order.getCustomer()).build());
            itemValues.put("Items", AttributeValue.builder().s(order.getItems().toString()).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(ordersTable)
                    .item(itemValues)
                    .build();

            dynamoDbClient.putItem(request);

            logger.info("Order ID: {} saved to DynamoDB.", order.getId());
        } catch (Exception e) {
            logger.error("Failed to save order ID: {} to DynamoDB.", order.getId(), e);
            throw e; // Rethrow or handle exception based on your needs
        }
    }

    private void sendOrderToSQS(Order order) {
        try {
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .messageBody("Order processed: " + order.getId())
                    .build();

            sqsClient.sendMessage(sendMessageRequest);

            logger.info("Order ID: {} sent to SQS.", order.getId());
        } catch (Exception e) {
            logger.error("Failed to send order ID: {} to SQS.", order.getId(), e);
            throw e; // Rethrow or handle exception based on your needs
        }
    }
}
