package hu.porkolab.chaosSymphony.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import hu.porkolab.chaosSymphony.orchestrator.kafka.InventoryRequestProducer;
import hu.porkolab.chaosSymphony.orchestrator.kafka.PaymentResultListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*; // Szükséges import a mockoláshoz

class OrchestratorFlowSmokeTest {

	@Test
	void paymentChargedTriggersInventoryRequest() throws Exception { // A throws Exception is kell
		// 1. Függőségek előkészítése (mockolás)
		var reg = new SimpleMeterRegistry();
		var failed = Counter.builder("orders.failed").register(reg);
		var idempotencyStore = mock(IdempotencyStore.class);
		var inventoryProducer = mock(InventoryRequestProducer.class);

		// Beállítjuk, hogy az idempotencia ellenőrzés mindig "true"-t adjon vissza
		when(idempotencyStore.markIfFirst(anyString())).thenReturn(true);

		// 2. Listener létrehozása a mockolt függőségekkel
		var listener = new PaymentResultListener(new ObjectMapper(), idempotencyStore, inventoryProducer, failed);

		// 3. Teszt üzenet létrehozása és a listener meghívása
		String payload = "{\"eventId\":\"e1\",\"orderId\":\"o1\",\"type\":\"PaymentResult\",\"payload\":\"{\\\"status\\\":\\\"CHARGED\\\", \\\"orderId\\\":\\\"o1\\\"}\"}";
		listener.onPaymentResult(new ConsumerRecord<>("payment.result", 0, 0, "o1", payload));

		// 4. Ellenőrzés: A sikeres fizetés után meghívódott-e az inventory producer?
		verify(inventoryProducer, times(1)).sendRequest(eq("o1"), anyString());
		assertEquals(0.0, failed.count(), "A sikeres fizetés nem növelheti a hibaszámlálót.");
	}
}