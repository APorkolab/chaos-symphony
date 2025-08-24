package hu.porkolab.chaosSymphony.orchestrator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {
	private final KafkaTemplate<String, String> kafka;

	public void sendPaymentRequested(String orderId, String payloadJson) {
		kafka.send("payment.requested", orderId, payloadJson);
	}
}
