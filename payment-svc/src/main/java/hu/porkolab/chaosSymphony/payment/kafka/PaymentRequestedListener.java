package hu.porkolab.chaosSymphony.payment.kafka;

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

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestedListener {

	private final PaymentResultProducer producer;
	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "payment.requested", groupId = "payment-requested")
	@Transactional
	public void onPaymentRequested(ConsumerRecord<String, String> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		EventEnvelope env = EnvelopeHelper.parse(rec.value());
		String orderId = env.getOrderId();

		JsonNode msg = om.readTree(env.getPayload());
		double amount = msg.path("amount").asDouble();

		// Simulate payment processing
		boolean success = ThreadLocalRandom.current().nextDouble() < 0.9;
		String status = success ? "CHARGED" : "CHARGE_FAILED";

		String resultPayload = om.createObjectNode()
				.put("orderId", orderId)
				.put("status", status)
				.put("amount", amount)
				.toString();

		log.info("Payment processed for orderId={}, status: {}", orderId, status);
		producer.sendResult(orderId, resultPayload);
	}
}
