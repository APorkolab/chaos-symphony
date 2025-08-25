package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class OrderCreatedListener {
	private final PaymentProducer producer;
	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "ordersdb.public.order_outbox", groupId = "orchestrator-order-created")
	@Transactional
	public void onOutbox(ConsumerRecord<String, String> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		JsonNode root = om.readTree(rec.value());
		String type = root.path("type").asText("");
		if (!"OrderCreated".equals(type)) {
			log.debug("Skipping non-OrderCreated event type: {}", type);
			return;
		}

		String payload = root.path("payload").asText("{}");
		JsonNode pay = om.readTree(payload);
		String orderId = pay.path("orderId").asText();

		log.info("OrderCreated received for orderId={} -> sending PaymentRequested", orderId);

		String paymentPayload = om.createObjectNode()
				.put("orderId", orderId)
				.put("amount", pay.path("total").asDouble())
				.put("currency", "HUF")
				.toString();

		producer.sendPaymentRequested(orderId, paymentPayload);
	}
}
