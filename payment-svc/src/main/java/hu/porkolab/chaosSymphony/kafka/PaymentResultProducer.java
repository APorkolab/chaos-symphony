package hu.porkolab.chaosSymphony.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultProducer {

	private final KafkaTemplate<String, String> kafka;

	public PaymentResultProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendResult(String orderId, String resultPayloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "PaymentResult", resultPayloadJson);
		kafka.send("payment.result", orderId, msg);
	}
}
