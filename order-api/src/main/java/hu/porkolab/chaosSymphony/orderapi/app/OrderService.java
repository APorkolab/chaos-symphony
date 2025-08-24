package hu.porkolab.chaosSymphony.orderapi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orders;
	private final OrderOutboxRepository outbox;
	private final ObjectMapper om = new ObjectMapper();

	@Transactional
	public UUID createOrder(CreateOrder cmd) {
		UUID orderId = UUID.randomUUID();

		orders.save(Order.builder()
				.id(orderId)
				.status("NEW")
				.total(cmd.total())
				.createdAt(Instant.now())
				.build());

		String payload = toJson(Map.of(
				"orderId", orderId.toString(),
				"total", cmd.total(),
				"currency", "HUF"));

		outbox.save(OrderOutbox.builder()
				.id(UUID.randomUUID())
				.aggregateId(orderId)
				.type("OrderCreated")
				.payload(payload)
				.occurredAt(Instant.now())
				.published(false)
				.build());

		return orderId;
	}

	private String toJson(Object o) {
		try {
			return om.writeValueAsString(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
