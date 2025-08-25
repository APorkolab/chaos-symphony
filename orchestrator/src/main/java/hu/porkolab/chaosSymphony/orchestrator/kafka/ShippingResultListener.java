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
public class ShippingResultListener {

	private static final Logger logger = LoggerFactory.getLogger(ShippingResultListener.class);

	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om;

	public ShippingResultListener(IdempotencyStore idempotencyStore, ObjectMapper om) {
		this.idempotencyStore = idempotencyStore;
		this.om = om;
	}

	@KafkaListener(topics = "shipping.result", groupId = "orchestrator-1")
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

			logger.info("Shipping result received: orderId={}, status={}", orderId, status);

			switch (status) {
				case "DELIVERED", "SHIPPED" -> logger.debug("Order {} successfully {}", orderId, status.toLowerCase());
				case "FAILED" -> logger.warn("Shipping FAILED for orderId={}", orderId);
				default -> logger.warn("Unknown shipping status='{}' for orderId={}", status, orderId);
			}

		} catch (Exception e) {
			logger.error("ShippingResult processing failed: {}", rec.value(), e);
			throw e;
		}
	}
}
