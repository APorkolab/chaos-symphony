package hu.porkolab.chaosSymphony.orderapi.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestProducer {

	private final KafkaTemplate<String, String> kafka;

	public PaymentRequestProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendRequest(String orderId, String payloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "PaymentRequested", payloadJson);
		kafka.send("payment.requested", orderId, msg);
	}
}
