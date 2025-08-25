package hu.porkolab.chaosSymphony.inventory.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRequestedListener {

	private final InventoryResultProducer producer;
	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "inventory.requested", groupId = "inventory-requested")
	@Transactional
	public void onInventoryRequested(ConsumerRecord<String, String> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		EventEnvelope env = EnvelopeHelper.parse(rec.value());
		String orderId = env.getOrderId();

		JsonNode msg = om.readTree(env.getPayload());
		int items = msg.path("items").asInt(1);

		boolean success = items > 0;
		String status = success ? "RESERVED" : "OUT_OF_STOCK";

		String resultPayload = om.createObjectNode()
				.put("orderId", orderId)
				.put("status", status)
				.put("items", items)
				.toString();

		log.info("Inventory processed for orderId={}, status: {}", orderId, status);
		producer.sendResult(orderId, resultPayload);
	}
}
