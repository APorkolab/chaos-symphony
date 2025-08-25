package hu.porkolab.chaosSymphony.shipping.kafka;

import hu.porkolab.chaosSymphony.common.chaos.ChaosProducer;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShippingResultProducer {
	private static final Logger log = LoggerFactory.getLogger(ShippingResultProducer.class);
	private final ChaosProducer chaos;

	public ShippingResultProducer(ChaosProducer chaos) {
		this.chaos = chaos;
	}

	public void sendResult(String orderId, String payloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "ShippingResult", payloadJson);
		chaos.send("shipping.result", orderId, "ShippingResult", msg);
		log.info("[SHIPPING] queued shipping.result key={}", orderId);
	}
}