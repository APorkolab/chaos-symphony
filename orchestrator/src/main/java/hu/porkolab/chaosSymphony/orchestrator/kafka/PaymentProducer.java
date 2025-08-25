package hu.porkolab.chaosSymphony.orchestrator.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentProducer {
	private final KafkaTemplate<String, String> kafka = null;

	public void sendPaymentRequested(String orderId, String paymentPayloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "PaymentRequested", paymentPayloadJson);
		kafka.send("payment.requested", orderId, msg);
	}
}