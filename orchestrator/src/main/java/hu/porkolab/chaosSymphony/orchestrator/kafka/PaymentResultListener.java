package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultListener {
	private static final Logger logger = LoggerFactory.getLogger(PaymentResultListener.class);
	private final InventoryRequestProducer inventoryProducer;
	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om = new ObjectMapper();

	public PaymentResultListener(InventoryRequestProducer inventoryProducer, IdempotencyStore idempotencyStore) {
		this.idempotencyStore = idempotencyStore;
		this.inventoryProducer = inventoryProducer;
	}

	@KafkaListener(topics = "payment.result", groupId = "orchestrator-1")
	public void onResult(ConsumerRecord<String, String> rec) throws Exception {
		try {
			// Itt kell kinyerni az env-t!
			EventEnvelope env = EnvelopeHelper.parse(rec.value());

			// Idempotency check
			if (!idempotencyStore.markIfFirst(env.getEventId())) {
				logger.debug("Skip duplicate eventId={}", env.getEventId());
				return;
			}

			String orderId = env.getOrderId();
			JsonNode msg = om.readTree(env.getPayload());
			String status = msg.path("status").asText();

			logger.info("Orchestrator got PaymentResult: orderId={}, status={}", orderId, status);

			if ("CHARGED".equals(status)) {
				String payload = om.createObjectNode()
						.put("orderId", orderId)
						.put("items", 2) 
						.toString();
				inventoryProducer.sendRequest(orderId, payload);
			}
		} catch (Exception e) {
			logger.error("PaymentResult processing failed: {}", rec.value(), e);
			throw e; // így a DefaultErrorHandler mehet DLQ-ba
		}
	}
}
