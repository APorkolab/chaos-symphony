package hu.porkolab.chaosSymphony.payment.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import hu.porkolab.chaosSymphony.payment.store.PaymentStatusStore;
import io.micrometer.core.instrument.Counter;
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
	private final PaymentStatusStore paymentStatusStore;
	private final Counter paymentsProcessedMain;
	private final Counter paymentsProcessedCanary;
	private final ObjectMapper om = new ObjectMapper();

	@KafkaListener(topics = "${kafka.topic.payment.requested}", groupId = "${kafka.group.id.payment}")
	@Transactional
	public void onPaymentRequested(ConsumerRecord<String, String> rec) throws Exception {
		paymentsProcessedMain.increment();
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
		paymentStatusStore.save(orderId, status);

		String resultPayload = om.createObjectNode()
				.put("orderId", orderId)
				.put("status", status)
				.put("amount", amount)
				.toString();

		log.info("Payment processed for orderId={}, status: {}", orderId, status);
		producer.sendResult(orderId, resultPayload);
	}

	@KafkaListener(topics = "${kafka.topic.payment.requested.canary}", groupId = "${kafka.group.id.payment.canary}")
	@Transactional
	public void onPaymentRequestedCanary(ConsumerRecord<String, String> rec) throws Exception {
		paymentsProcessedCanary.increment();
		log.info("[CANARY] Received payment request for key: {}", rec.key());
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("[CANARY] Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		EventEnvelope env = EnvelopeHelper.parse(rec.value());
		String orderId = env.getOrderId();

		JsonNode msg = om.readTree(env.getPayload());
		double amount = msg.path("amount").asDouble();

		// Simulate payment processing
		boolean success = ThreadLocalRandom.current().nextDouble() < 0.9;
		String status = success ? "CHARGED" : "CHARGE_FAILED";
		paymentStatusStore.save(orderId, status);

		String resultPayload = om.createObjectNode()
				.put("orderId", orderId)
				.put("status", status)
				.put("amount", amount)
				.toString();

		log.info("[CANARY] Payment processed for orderId={}, status: {}", orderId, status);
		producer.sendResult(orderId, resultPayload);
	}
}
