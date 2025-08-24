package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentResultListener {
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "payment.result", groupId = "orchestrator-1")
	public void onResult(ConsumerRecord<String, String> rec) {
		try {
			JsonNode msg = om.readTree(rec.value());
			String orderId = msg.path("orderId").asText();
			String status = msg.path("status").asText();
			log.info("PaymentResult for orderId={} -> {}", orderId, status);
			// Holnap: if CHARGED -> inventory.requested, else -> compensate (refund)
		} catch (Exception e) {
			log.error("PaymentResult parse error: {}", rec.value(), e);
		}
	}
}
