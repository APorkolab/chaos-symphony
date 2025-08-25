package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import io.micrometer.core.instrument.Counter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultListener {

	private final ObjectMapper om = new ObjectMapper();

	private final Counter ordersSucceeded;
	private final Counter ordersFailed;

	public PaymentResultListener(Counter ordersSucceeded,
			Counter ordersFailed) {
		this.ordersSucceeded = ordersSucceeded;
		this.ordersFailed = ordersFailed;
	}

	@KafkaListener(topics = "payment.result", groupId = "orchestrator-1")
	public void onPaymentResult(ConsumerRecord<String, String> rec) {
		try {
			EventEnvelope env = EnvelopeHelper.parse(rec.value());
			JsonNode p = om.readTree(env.getPayload());
			String status = p.path("status").asText("UNKNOWN");

			if ("CHARGED".equalsIgnoreCase(status)) {
				ordersSucceeded.increment();
			} else {
				ordersFailed.increment();
			}
		} catch (Exception e) {
			ordersFailed.increment();
		}
	}
}
