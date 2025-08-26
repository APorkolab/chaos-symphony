package hu.porkolab.chaosSymphony.orchestrator;

import hu.porkolab.chaosSymphony.orchestrator.kafka.PaymentResultListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import hu.porkolab.chaosSymphony.common.idemp.NoopIdempotencyStore;

import static org.junit.jupiter.api.Assertions.*;

class OrchestratorFlowSmokeTest {

	@Test
	void paymentChargedIncrementsSucceeded() throws Exception {
		var reg = new SimpleMeterRegistry();
		Counter succeeded = Counter.builder("orders.succeeded").register(reg);
		Counter failed = Counter.builder("orders.failed").register(reg);

		var l = new PaymentResultListener(new NoopIdempotencyStore(), succeeded, failed);
		String payload = "{\"eventId\":\"e1\",\"correlationId\":\"c1\",\"payload\":\"{\\\"status\\\":\\\"CHARGED\\\",\\\"orderId\\\":\\\"o1\\\"}\"}";
		l.onPaymentResult(new ConsumerRecord<>("payment.result", 0, 0, "o1", payload));
		assertEquals(1.0, succeeded.count(), 0.001);
	}
}
