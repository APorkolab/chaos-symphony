package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestedListener {
	private final PaymentResultProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "payment.requested", groupId = "payment-1")
	public void onPaymentRequested(ConsumerRecord<String, String> rec) {
		try {
			JsonNode msg = om.readTree(rec.value());
			String orderId = msg.path("orderId").asText();
			double amount = msg.path("amount").asDouble();

			// MVP: 90% siker, 10% hiba (később káosz-szabály vezérli)
			boolean success = ThreadLocalRandom.current().nextDouble() < 0.9;

			String result = om.createObjectNode()
					.put("orderId", orderId)
					.put("status", success ? "CHARGED" : "CHARGE_FAILED")
					.put("amount", amount)
					.toString();

			log.info("Payment {} for orderId={}", success ? "OK" : "FAIL", orderId);
			producer.sendResult(orderId, result);
		} catch (Exception e) {
			log.error("PaymentRequested processing failed: {}", rec.value(), e);
			// MVP: később retry/DLQ
		}
	}
}
