package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import hu.porkolab.chaosSymphony.events.OrderCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {
	private final PaymentProducer producer;
	private final IdempotencyStore idempotencyStore;
	private final ObjectMapper om;

	@KafkaListener(topics = "order.created", groupId = "orchestrator-order-created")
	@Transactional
	public void onOrderCreated(ConsumerRecord<String, OrderCreated> rec) throws Exception {
		if (!idempotencyStore.markIfFirst(rec.key())) {
			log.warn("Duplicate message detected, skipping: {}", rec.key());
			return;
		}

		OrderCreated event = rec.value();
		String orderId = event.orderId().toString();

		log.info("OrderCreated received for orderId={} -> sending PaymentRequested", orderId);

		ObjectNode paymentPayloadNode = om.createObjectNode();
		paymentPayloadNode.put("orderId", orderId);
		paymentPayloadNode.set("amount", om.getNodeFactory().numberNode(event.total()));
		paymentPayloadNode.put("currency", event.currency());

		producer.sendPaymentRequested(orderId, paymentPayloadNode.toString());
	}
}