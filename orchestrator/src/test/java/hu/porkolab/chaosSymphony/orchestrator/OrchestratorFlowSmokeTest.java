package hu.porkolab.chaosSymphony.orchestrator;

import hu.porkolab.chaosSymphony.orchestrator.kafka.PaymentResultListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrchestratorFlowSmokeTest {

	@Test
	void paymentChargedIncrementsSucceeded() {
		var reg = new SimpleMeterRegistry();
		Counter succeeded = Counter.builder("orders.succeeded").register(reg);
		Counter failed = Counter.builder("orders.failed").register(reg);

		var l = new PaymentResultListener(succeeded, failed);
		String payload = "{\"eventId\":\"e1\",\"orderId\":\"o1\",\"type\":\"PaymentResult\",\"payload\":\"{\\\"status\\\":\\\"CHARGED\\\"}\"}";
		l.onPaymentResult(new ConsumerRecord<>("payment.result", 0, 0, "o1", payload));
		assertEquals(1.0, succeeded.count(), 0.001);
	}
}
