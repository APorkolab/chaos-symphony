package hu.porkolab.chaosSymphony.inventory.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryRequestedListener {

	private final InventoryResultProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	public InventoryRequestedListener(InventoryResultProducer producer) {
		this.producer = producer;
	}

	@KafkaListener(topics = "inventory.requested", groupId = "inventory-1")
	public void onInventoryRequested(ConsumerRecord<String, String> rec) {
		var env = EnvelopeHelper.parse(rec.value());
		var orderId = env.getOrderId();

		if ("BREAK-ME".equals(orderId)) {
			throw new RuntimeException("boom-test");
		}
		// Simulate processing the inventory request
		try {
		//	EventEnvelope env = EnvelopeHelper.parse(rec.value());
		//	String orderId = env.getOrderId();

			JsonNode msg = om.readTree(env.getPayload());
			int items = msg.path("items").asInt(1);

			boolean success = items > 0;

			String resultPayload = om.createObjectNode()
					.put("orderId", orderId)
					.put("status", success ? "RESERVED" : "OUT_OF_STOCK")
					.put("items", items)
					.toString();

			producer.sendResult(orderId, resultPayload);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
