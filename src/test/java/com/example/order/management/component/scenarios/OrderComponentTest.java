package com.example.order.management.component.scenarios;

import com.example.order.management.component.BaseComponentTest;
import com.example.order.management.exception.PaymentException;
import com.example.order.management.model.Order;
import com.example.order.management.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderComponentTest extends BaseComponentTest {

    @Autowired
    private OrderService orderService;

    @Test
    void testProcessOrderSuccess() {
        final var order = createTestOrder();

        when(paymentService.processPayment(order.getPaymentDetails())).thenReturn(true);

        orderService.processOrder(order);

        verify(paymentService).processPayment(order.getPaymentDetails());
        verify(inventoryService).updateInventory(order.getItems());

        // Explicitly capture the PutItemRequest
        var putItemRequestCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        var capturedPutItemRequest = putItemRequestCaptor.getValue();
        verifyPutItemRequest(capturedPutItemRequest, order);

        // Explicitly capture the SendMessageRequest
        var sendMessageRequestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(sendMessageRequestCaptor.capture());
        var capturedSendMessageRequest = sendMessageRequestCaptor.getValue();
        verifySendMessageRequest(capturedSendMessageRequest, order);

        verify(notificationService).sendOrderConfirmation(order);
    }

    @Test
    void testProcessOrderPaymentFailure() {
        final var order = createTestOrder();

        when(paymentService.processPayment(order.getPaymentDetails())).thenReturn(false);

        assertThrows(PaymentException.class, () -> orderService.processOrder(order));

        verify(paymentService).processPayment(order.getPaymentDetails());
        verify(inventoryService, never()).updateInventory(any());
        verify(dynamoDbClient, never()).putItem(any(PutItemRequest.class));
        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
        verify(notificationService, never()).sendOrderConfirmation(any());
    }

    private Order createTestOrder() {
        final var order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer("John Doe");
        order.setItems(List.of("item1", "item2"));
        return order;
    }

}
