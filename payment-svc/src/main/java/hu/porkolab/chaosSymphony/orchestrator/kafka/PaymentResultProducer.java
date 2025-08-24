package hu.porkolab.chaosSymphony.orchestrator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultProducer {
	private final KafkaTemplate<String, String> kafka;

	public void sendResult(String orderId, String resultJson) {
		kafka.send("payment.result", orderId, resultJson);
	}
}
