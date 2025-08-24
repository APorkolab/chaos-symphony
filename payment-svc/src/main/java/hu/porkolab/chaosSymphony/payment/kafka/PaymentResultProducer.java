package hu.porkolab.chaosSymphony.payment.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultProducer {

	private final KafkaTemplate<String, String> kafka;

	public PaymentResultProducer(KafkaTemplate<String, String> kafka) {
		this.kafka = kafka;
	}

	public void sendResult(String orderId, String resultPayloadJson) {
		try {
			String msg = EnvelopeHelper.envelope(orderId, "PaymentResult", resultPayloadJson);
			RecordMetadata md = kafka.send("payment.result", orderId, msg).get().getRecordMetadata();
			System.out.println("[PAYMENT] SENT payment.result key=" + orderId +
					" -> " + md.topic() + "-" + md.partition() + "@" + md.offset());
		} catch (Exception e) {
			System.err.println("[PAYMENT] SEND FAIL payment.result key=" + orderId + " err=" + e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
