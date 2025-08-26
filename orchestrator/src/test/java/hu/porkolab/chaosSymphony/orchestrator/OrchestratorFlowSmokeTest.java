package hu.porkolab.chaosSymphony.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.idemp.NoopIdempotencyStore;
import hu.porkolab.chaosSymphony.orchestrator.kafka.InventoryRequestProducer;
import hu.porkolab.chaosSymphony.orchestrator.kafka.PaymentResultListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrchestratorFlowSmokeTest {

    @Test
    void paymentChargedTriggersInventoryRequest() throws Exception {
        // Arrange
        var reg = new SimpleMeterRegistry();
        Counter failed = Counter.builder("orders.failed").register(reg);
        var mockedProducer = mock(InventoryRequestProducer.class);
        var objectMapper = new ObjectMapper();

        var listener = new PaymentResultListener(objectMapper, new NoopIdempotencyStore(), mockedProducer, failed);
        String payload = "{\"eventId\":\"e1\",\"correlationId\":\"c1\",\"payload\":\"{\\\"status\\\":\\\"CHARGED\\\",\\\"orderId\\\":\\\"o1\\\"}\"}";

        // Act
        listener.onPaymentResult(new ConsumerRecord<>("payment.result", 0, 0, "o1", payload));

        // Assert
        verify(mockedProducer, times(1)).sendRequest(eq("o1"), anyString());
        assertEquals(0.0, failed.count());
    }

    @Test
    void paymentFailedIncrementsFailedCounter() throws Exception {
        // Arrange
        var reg = new SimpleMeterRegistry();
        Counter failed = Counter.builder("orders.failed").register(reg);
        var mockedProducer = mock(InventoryRequestProducer.class);
        var objectMapper = new ObjectMapper();

        var listener = new PaymentResultListener(objectMapper, new NoopIdempotencyStore(), mockedProducer, failed);
        String payload = "{\"eventId\":\"e1\",\"correlationId\":\"c1\",\"payload\":\"{\\\"status\\\":\\\"CHARGE_FAILED\\\",\\\"orderId\\\":\\\"o1\\\"}\"}";

        // Act
        listener.onPaymentResult(new ConsumerRecord<>("payment.result", 0, 0, "o1", payload));

        // Assert
        verify(mockedProducer, never()).sendRequest(anyString(), anyString());
        assertEquals(1.0, failed.count());
    }
}
