package hu.porkolab.chaosSymphony.orderapi.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
	private final KafkaTemplate<String, String> kafka;

	public void sendOutbox(String orderId, String outboxJson) {
		kafka.send("ordersdb.public.order_outbox", orderId, outboxJson);
	}
}
