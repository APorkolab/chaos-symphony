package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultListener {

	private final ObjectMapper om = new ObjectMapper();
	private final IdempotencyStore idempotencyStore;
	private final Counter ordersSucceeded;
	private final Counter ordersFailed;

	@KafkaListener(topics = "payment.result", groupId = "orchestrator-payment-result")
	@Transactional
	public void onPaymentResult(ConsumerRecord<String, String> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		EventEnvelope env = EnvelopeHelper.parse(rec.value());
		JsonNode p = om.readTree(env.getPayload());
		String status = p.path("status").asText("UNKNOWN");
		String orderId = p.path("orderId").asText(null);

		if ("CHARGED".equalsIgnoreCase(status)) {
			log.info("Payment successful for orderId={}", orderId);
			ordersSucceeded.increment();
		} else {
			log.error("Payment failed for orderId={}", orderId);
			ordersFailed.increment();
		}
	}
}
