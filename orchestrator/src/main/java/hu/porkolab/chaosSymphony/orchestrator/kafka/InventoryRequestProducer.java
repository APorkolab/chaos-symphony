package hu.porkolab.chaosSymphony.orchestrator.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryRequestProducer {

	private final KafkaTemplate<String, String> kafka;

	public InventoryRequestProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendRequest(String orderId, String payloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "InventoryRequested", payloadJson);
		kafka.send("inventory.requested", orderId, msg);
	}
}
