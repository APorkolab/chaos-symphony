package hu.porkolab.chaosSymphony.shipping.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShippingResultProducer {

	private final KafkaTemplate<String, String> kafka;

	public ShippingResultProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendResult(String orderId, String resultPayloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "ShippingResult", resultPayloadJson);
		kafka.send("shipping.result", orderId, msg);
	}
}
