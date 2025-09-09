package hu.porkolab.chaosSymphony.orderapi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.events.OrderCreated;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderOutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class OrderServiceIntegrationTest {

    private final OrderService orderService;
    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    OrderServiceIntegrationTest(OrderService orderService, OrderOutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Test
    void createOrder_shouldSaveOrderAndOutboxEventAtomically() throws Exception {
        // Given
        String customerId = "test-customer-123";
        CreateOrder command = new CreateOrder(customerId, BigDecimal.valueOf(99.99), "USD");

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
