package hu.porkolab.chaosSymphony.shipping.kafka;

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
public class ShippingRequestedListener {

	private final ShippingResultProducer producer;
	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "shipping.requested", groupId = "shipping-requested")
	@Transactional
	public void onShippingRequested(ConsumerRecord<String, String> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		EventEnvelope env = EnvelopeHelper.parse(rec.value());
		String orderId = env.getOrderId();

		JsonNode msg = om.readTree(env.getPayload());
		String address = msg.path("address").asText("UNKNOWN");

		boolean success = !"UNKNOWN".equals(address);
		String status = success ? "DELIVERED" : "DELIVERY_FAILED";

		String resultPayload = om.createObjectNode()
				.put("orderId", orderId)
				.put("status", status)
				.put("address", address)
				.toString();

		log.info("Shipping processed for orderId={}, status: {}", orderId, status);
		producer.sendResult(orderId, resultPayload);
	}
}
