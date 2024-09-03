package com.example.order.management.component;

import com.example.order.management.OrderManagementApplication;
import com.example.order.management.model.Order;
import com.example.order.management.service.InventoryService;
import com.example.order.management.service.NotificationService;
import com.example.order.management.service.PaymentService;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = OrderManagementApplication.class)
@ActiveProfiles("test")
public abstract class BaseComponentTest {

    @Value("${orders.table.name}")
    private String ordersTable;

    @Value("${sqs.queue.url}")
    private String sqsQueueUrl;

    // Mock Beans for AWS clients
    @MockBean
    protected DynamoDbClient dynamoDbClient;

    @MockBean
    protected SqsClient sqsClient;

    // Mock Beans for services
    @MockBean
    protected PaymentService paymentService;

    @MockBean
    protected InventoryService inventoryService;

    @MockBean
    protected NotificationService notificationService;

    protected PutItemRequest capturePutItemRequest() {
        final var putItemRequestCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        return putItemRequestCaptor.getValue();
    }

    protected SendMessageRequest captureSendMessageRequest() {
        final var sendMessageRequestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(sendMessageRequestCaptor.capture());
        return sendMessageRequestCaptor.getValue();
    }

    protected void verifyPutItemRequest(PutItemRequest request, Order order) {
        assertNotNull(request, "PutItemRequest should not be null");
        assertEquals(ordersTable, request.tableName(), "Table name should match");
        assertNotNull(request.item(), "Item map should not be null");
        assertEquals(order.getId().toString(), request.item().get("OrderId").s(), "OrderId should match");
        assertEquals(order.getCustomer(), request.item().get("Customer").s(), "Customer should match");
        assertEquals(order.getItems().toString(), request.item().get("Items").s(), "Items should match");
    }

    protected void verifySendMessageRequest(SendMessageRequest request, Order order) {
        assertNotNull(request, "SendMessageRequest should not be null");
        assertEquals(sqsQueueUrl, request.queueUrl(), "Queue URL should match");
        assertEquals("Order processed: " + order.getId(), request.messageBody(), "Message body should match");
    }

}
