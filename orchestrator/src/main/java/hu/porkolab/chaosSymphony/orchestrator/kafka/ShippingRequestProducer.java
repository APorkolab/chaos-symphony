package hu.porkolab.chaosSymphony.orchestrator.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShippingRequestProducer {

	private final KafkaTemplate<String, String> kafka;

	public ShippingRequestProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendRequest(String orderId, String payloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "ShippingRequested", payloadJson);
		kafka.send("shipping.requested", orderId, msg);
	}
}
