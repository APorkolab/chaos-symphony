package hu.porkolab.chaosSymphony.orderapi.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.events.OrderCreated;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Transactional
    public UUID createOrder(CreateOrder cmd) {
        UUID orderId = UUID.randomUUID();
        BigDecimal total = cmd.total().setScale(2, BigDecimal.ROUND_HALF_UP);
        Instant now = Instant.now(clock);

        // 1. Save the business entity (Order)
        orderRepository.save(Order.builder()
                .id(orderId)
                .status(OrderStatus.NEW)
                .total(total)
                .createdAt(now)
                .build());

        // 2. Create the Avro event
        OrderCreated eventPayload = OrderCreated.newBuilder()
                .setOrderId(orderId.toString())
                .setTotal(total.doubleValue())
                .setCurrency("HUF")
                .setCustomerId(cmd.customerId())
                .build();

        // 3. Create and save the Outbox event within the same transaction
        try {
            String payloadJson = objectMapper.writeValueAsString(eventPayload);
            OrderOutbox outboxEvent = OrderOutbox.builder()
                    .id(UUID.randomUUID())
                    .aggregateId(orderId)
                    .type(eventPayload.getClass().getSimpleName())
                    .payload(payloadJson)
                    .occurredAt(now)
                    .build();
            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderCreated event for outbox", e);
            // In a real application, this should probably cause the transaction to roll back.
            throw new RuntimeException("Failed to serialize event payload", e);
        }

        log.info("Order {} created and outbox event saved for Debezium", orderId);
        return orderId;
    }
}
