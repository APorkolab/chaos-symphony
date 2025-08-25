package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryResultListener {

	private final IdempotencyStore idempotencyStore;
	private final ShippingRequestProducer shippingProducer;
	private final ObjectMapper om;
	private final Counter ordersSucceeded;
	private final Counter ordersFailed;

	@KafkaListener(topics = "inventory.result", groupId = "orchestrator-inventory-result")
	@Transactional
	public void onResult(ConsumerRecord<String, String> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		EventEnvelope env = EnvelopeHelper.parse(rec.value());
		String orderId = env.getOrderId();
		JsonNode msg = om.readTree(env.getPayload());
		String status = msg.path("status").asText("");

		log.info("Orchestrator got InventoryResult: orderId={}, status={}", orderId, status);

		switch (status) {
			case "RESERVED" -> {
				String payload = om.createObjectNode()
						.put("orderId", orderId)
						.put("address", "Budapest")
						.toString();
				shippingProducer.sendRequest(orderId, payload);
				log.debug("Shipping request sent for orderId={}", orderId);
				ordersSucceeded.increment();
			}
			case "OUT_OF_STOCK" -> {
				log.warn("Inventory OUT_OF_STOCK for orderId={}", orderId);
				ordersFailed.increment();
			}
			default -> {
				log.warn("Unknown inventory status='{}' for orderId={}", status, orderId);
				ordersFailed.increment();
			}
		}
	}
}
