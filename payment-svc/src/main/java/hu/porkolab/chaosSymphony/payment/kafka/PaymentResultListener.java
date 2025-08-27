package hu.porkolab.chaosSymphony.payment.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultListener {

	private final ObjectMapper om = new ObjectMapper();
	// Új függőségek a következő lépések elindításához
	private final InventoryRequestProducer inventoryProducer;
	private final OrderCompensationProducer compensationProducer;

	@KafkaListener(topics = "payment.result", groupId = "orchestrator-1")
	public void onResult(ConsumerRecord<String, String> rec) {
		try {
			EventEnvelope env = EnvelopeHelper.parse(rec.value());
			String orderId = env.getOrderId();

			JsonNode msg = om.readTree(env.getPayload());
			String status = msg.path("status").asText();

			log.info("PaymentResult received for orderId={} with status={}", orderId, status);

			if ("CHARGED".equalsIgnoreCase(status)) {
				// Sikeres fizetés: továbbküldjük a kérést a készletkezelőnek
				ObjectNode inventoryPayload = om.createObjectNode()
						.put("orderId", orderId);
				// Itt további termékinformációkat is átadhatnánk
				inventoryProducer.sendRequest(orderId, inventoryPayload.toString());
				log.info("Inventory reservation requested for orderId={}", orderId);
			} else {
				// Sikertelen fizetés: kompenzációs eseményt küldünk
				ObjectNode compensationPayload = om.createObjectNode()
						.put("orderId", orderId)
						.put("reason", "PAYMENT_FAILED");
				compensationProducer.sendCompensation(orderId, compensationPayload.toString());
				log.warn("Payment failed, compensation requested for orderId={}", orderId);
			}

		} catch (Exception e) {
			log.error("Error processing PaymentResult message: {}", rec.value(), e);
		}
	}
}