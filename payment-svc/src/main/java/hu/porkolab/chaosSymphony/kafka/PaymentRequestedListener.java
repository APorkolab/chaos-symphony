package hu.porkolab.chaosSymphony.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentRequestedListener {

	private final PaymentResultProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	public PaymentRequestedListener(PaymentResultProducer producer) {
		this.producer = producer;
	}

	@KafkaListener(topics = "payment.requested", groupId = "payment-1")
	public void onPaymentRequested(ConsumerRecord<String, String> rec) {
		try {
			EventEnvelope env = EnvelopeHelper.parse(rec.value());
			String orderId = env.getOrderId();

			JsonNode msg = om.readTree(env.getPayload());
			double amount = msg.path("amount").asDouble();

			boolean success = ThreadLocalRandom.current().nextDouble() < 0.9;

			String resultPayload = om.createObjectNode()
					.put("orderId", orderId)
					.put("status", success ? "CHARGED" : "CHARGE_FAILED")
					.put("amount", amount)
					.toString();

			producer.sendResult(orderId, resultPayload);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
