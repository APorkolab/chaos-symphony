package hu.porkolab.chaosSymphony.orderapi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.orderapi.domain.OutboxEvent;
import hu.porkolab.chaosSymphony.orderapi.domain.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventProducer {

	private final OutboxRepository outboxRepository;
	private final ObjectMapper objectMapper;

	@SneakyThrows
	public void fire(Object event) {
		String payloadJson = objectMapper.writeValueAsString(event);
		String eventType = event.getClass().getSimpleName();

		// Feltételezzük, hogy az esemény tartalmaz egy orderId mezőt
		UUID orderId = (UUID) event.getClass().getMethod("orderId").invoke(event);

		EventEnvelope envelope = new EventEnvelope(
				UUID.randomUUID().toString(),
				orderId.toString(),
				eventType,
				payloadJson);

		String envelopeJson = objectMapper.writeValueAsString(envelope);

		OutboxEvent outboxEvent = new OutboxEvent();
		outboxEvent.setAggregateId(orderId.toString());
		outboxEvent.setAggregateType("Order");
		outboxEvent.setPayload(envelopeJson);
		outboxEvent.setType(eventType);

		outboxRepository.save(outboxEvent);
	}
}