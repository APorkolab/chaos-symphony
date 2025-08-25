package hu.porkolab.chaosSymphony.inventory.kafka;

import hu.porkolab.chaosSymphony.common.chaos.ChaosProducer;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultProducer {
	private static final Logger log = LoggerFactory.getLogger(InventoryResultProducer.class);
	private final ChaosProducer chaos;

	public InventoryResultProducer(ChaosProducer chaos) {
		this.chaos = chaos;
	}

	public void sendResult(String orderId, String payloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "InventoryResult", payloadJson);
		chaos.send("inventory.result", orderId, "InventoryResult", msg);
		log.info("[INVENTORY] queued inventory.result key={}", orderId);
	}
}