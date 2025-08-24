package hu.porkolab.chaosSymphony.inventory.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultProducer {

	private final KafkaTemplate<String, String> kafka;

	public InventoryResultProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendResult(String orderId, String resultPayloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "InventoryResult", resultPayloadJson);
		kafka.send("inventory.result", orderId, msg);
	}
}
