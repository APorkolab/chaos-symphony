package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultListener {

	private static final Logger logger = LoggerFactory.getLogger(InventoryResultListener.class);

	private final IdempotencyStore idempotencyStore;
	private final ShippingRequestProducer shippingProducer;
	private final ObjectMapper om;

	public InventoryResultListener(ShippingRequestProducer shippingProducer,
			IdempotencyStore idempotencyStore,
			ObjectMapper objectMapper) {
		this.idempotencyStore = idempotencyStore;
		this.shippingProducer = shippingProducer;
		this.om = objectMapper; // injektált, thread-safe, újrahasznosított
	}

	@KafkaListener(topics = "inventory.result", groupId = "orchestrator-1")
	public void onResult(ConsumerRecord<String, String> rec) throws Exception {
		try {
			EventEnvelope env = EnvelopeHelper.parse(rec.value());

			if (!idempotencyStore.markIfFirst(env.getEventId())) {
				logger.debug("Skip duplicate eventId={}", env.getEventId());
				return;
			}

			String orderId = env.getOrderId();
			JsonNode msg = om.readTree(env.getPayload());
			String status = msg.path("status").asText("");

			logger.info("Orchestrator got InventoryResult: orderId={}, status={}", orderId, status);

			switch (status) {
				case "RESERVED" -> {
					String payload = om.createObjectNode()
							.put("orderId", orderId)
							.put("address", "Budapest")
							.toString();
					shippingProducer.sendRequest(orderId, payload);
					logger.debug("Shipping request sent for orderId={}", orderId);
				}
				case "OUT_OF_STOCK" -> logger.warn("Inventory OUT_OF_STOCK for orderId={}", orderId);
				default -> logger.warn("Unknown inventory status='{}' for orderId={}", status, orderId);
			}

		} catch (Exception e) {
			logger.error("InventoryResult processing failed: {}", rec.value(), e);
			throw e; 
		}
	}
}
