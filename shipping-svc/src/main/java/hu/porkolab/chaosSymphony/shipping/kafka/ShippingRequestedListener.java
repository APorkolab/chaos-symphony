package hu.porkolab.chaosSymphony.shipping.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingRequestedListener {

	private final ShippingResultProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	public ShippingRequestedListener(ShippingResultProducer producer) {
		this.producer = producer;
	}

	@KafkaListener(topics = "shipping.requested", groupId = "shipping-1")
	public void onShippingRequested(ConsumerRecord<String, String> rec) {
		try {
			EventEnvelope env = EnvelopeHelper.parse(rec.value());
			String orderId = env.getOrderId();

			JsonNode msg = om.readTree(env.getPayload());
			String address = msg.path("address").asText("UNKNOWN");

			boolean success = !"UNKNOWN".equals(address);

			String resultPayload = om.createObjectNode()
					.put("orderId", orderId)
					.put("status", success ? "DELIVERED" : "DELIVERY_FAILED")
					.put("address", address)
					.toString();

			producer.sendResult(orderId, resultPayload);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
