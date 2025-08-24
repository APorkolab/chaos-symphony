package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingResultListener {

	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "shipping.result", groupId = "orchestrator-1")
	public void onResult(ConsumerRecord<String, String> rec) {
		try {
			EventEnvelope env = EnvelopeHelper.parse(rec.value());
			String orderId = env.getOrderId();
			JsonNode msg = om.readTree(env.getPayload());
			String status = msg.path("status").asText();

			System.out.println("Order " + orderId + " shipping finished with: " + status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
