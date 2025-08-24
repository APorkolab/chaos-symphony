package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {
	private final PaymentProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	/**
	 * Debezium `ExtractNewRecordState` miatt a value a sor JSON-ja:
	 * { id, aggregateId, type, payload, occurredAt, published }
	 */
	@KafkaListener(topics = "ordersdb.public.order_outbox", groupId = "orchestrator-1")
	public void onOutbox(ConsumerRecord<String, String> rec) {
		try {
			JsonNode root = om.readTree(rec.value());
			String type = root.path("type").asText("");
			if (!"OrderCreated".equals(type))
				return; // csak erre reagálunk

			String payload = root.path("payload").asText("{}");
			JsonNode pay = om.readTree(payload);
			String orderId = pay.path("orderId").asText();

			log.info("OrderCreated received for orderId={} -> sending PaymentRequested", orderId);
			// a payment.requested payload legyen minimális
			String paymentPayload = om.createObjectNode()
					.put("orderId", orderId)
					.put("amount", pay.path("total").asDouble())
					.put("currency", "HUF")
					.toString();

			producer.sendPaymentRequested(orderId, paymentPayload);
		} catch (Exception e) {
			log.error("Failed to process outbox record: {}", rec.value(), e);
			// MVP: egyelőre csak log; később retry/DLQ
		}
	}
}
