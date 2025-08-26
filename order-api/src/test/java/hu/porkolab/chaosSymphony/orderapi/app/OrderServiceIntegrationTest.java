package hu.porkolab.chaosSymphony.orderapi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.events.OrderCreated;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderOutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderOutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_shouldSaveOrderAndOutboxEventAtomically() throws Exception {
        // Given
        String customerId = "test-customer-123";
        CreateOrder command = new CreateOrder(BigDecimal.valueOf(99.99), customerId);

        // When
        UUID orderId = orderService.createOrder(command);

        // Then
        assertNotNull(orderId);

        // Verify that the outbox event was saved correctly
        var outboxEvents = outboxRepository.findAll();
        assertEquals(1, outboxEvents.size());

        var outboxEvent = outboxEvents.get(0);
        assertEquals(orderId, outboxEvent.getAggregateId());
        assertEquals("OrderCreated", outboxEvent.getType());

        // Verify the payload
        OrderCreated payload = objectMapper.readValue(outboxEvent.getPayload(), OrderCreated.class);
        assertEquals(orderId.toString(), payload.getOrderId());
        assertEquals(customerId, payload.getCustomerId());
        assertEquals(99.99, payload.getTotal());
    }
}
