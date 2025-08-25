package hu.porkolab.chaosSymphony.orderapi.app;

import hu.porkolab.chaosSymphony.events.OrderCreated;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.Order;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderRepository;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orders;
	private final KafkaTemplate<String, OrderCreated> kafkaTemplate;
	private final Clock clock;

	@Transactional
	public UUID createOrder(CreateOrder cmd) {
		UUID orderId = UUID.randomUUID();
		BigDecimal total = cmd.total().setScale(2, BigDecimal.ROUND_HALF_UP);

		// 1. Save the order to the database
		orders.save(Order.builder()
				.id(orderId)
				.status(OrderStatus.NEW)
				.total(total)
				.createdAt(Instant.now(clock))
				.build());

		// 2. Create the Avro event
		OrderCreated event = OrderCreated.newBuilder()
				.setOrderId(orderId.toString())
				.setTotal(total.doubleValue())
				.setCurrency("HUF")
				.build();

		// 3. Send the event directly to Kafka (Note: This breaks the transactional outbox guarantee)
		kafkaTemplate.send("order.created", orderId.toString(), event);

		log.info("Order {} created and OrderCreated event sent with total {}", orderId, total);
		return orderId;
	}
}
