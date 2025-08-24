package hu.porkolab.chaosSymphony.orderapi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orders;
	private final OrderOutboxRepository outbox;
	private final ObjectMapper om; // beanből
	private final Clock clock; // beanből (UTC)

	@Transactional
	public UUID createOrder(CreateOrder cmd) {
		UUID orderId = UUID.randomUUID();

		BigDecimal total = cmd.total().setScale(2, BigDecimal.ROUND_HALF_UP);

		orders.save(Order.builder()
				.id(orderId)
				.status(OrderStatus.NEW)
				.total(total)
				.createdAt(Instant.now(clock))
				.build());

		writeOutbox(orderId, "OrderCreated", Map.of(
				"orderId", orderId.toString(),
				"total", total,
				"currency", "HUF"));

		log.info("Order {} created with total {}", orderId, total);
		return orderId;
	}

	private void writeOutbox(UUID aggregateId, String type, Object payloadObj) {
		String payload = toJson(payloadObj);
		outbox.save(OrderOutbox.builder()
				.id(UUID.randomUUID())
				.aggregateId(aggregateId)
				.type(type)
				.payload(payload)
				.occurredAt(Instant.now(clock))
				.published(false)
				.build());
	}

	private String toJson(Object o) {
		try {
			return om.writeValueAsString(o);
		} catch (Exception e) {
			throw new IllegalStateException("JSON serialize failed", e);
		}
	}
}
