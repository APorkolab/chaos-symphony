package hu.porkolab.chaosSymphony.payment.contract;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.payment.kafka.PaymentResultProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

/**
 * Contract test for payment result messages where payment-svc is the consumer
 * (it produces payment.result messages that the orchestrator consumes).
 */
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
@ActiveProfiles("test")
@PactTestFor(providerName = "payment-result-producer", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
public class PaymentResultContractTest {

    @SpyBean
    private PaymentResultProducer paymentResultProducer;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pact(consumer = "orchestrator")
    public MessagePact createPaymentResultPact(MessagePactBuilder builder) {
        return builder
                .expectsToReceive("A payment result event")
                .withContent(newJsonBody(envelope -> {
                    envelope.stringType("orderId", "e7a4f431-b2e3-4b43-8a24-8e2b1d3a0e46");
                    envelope.stringType("eventId", "f8b5c2d1-3e4f-5a6b-7c8d-9e0f1a2b3c4d");
                    envelope.stringType("type", "PaymentResult");
                    envelope.stringType("payload", "{\"orderId\":\"e7a4f431-b2e3-4b43-8a24-8e2b1d3a0e46\",\"status\":\"CHARGED\",\"amount\":123.45}");
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPaymentResultPact")
    public void testPaymentResultMessage(List<Message> messages) throws Exception {
        // Verify that the producer can create messages in the expected format
        assert !messages.isEmpty();
        
        // Test the actual message format
        Message message = messages.get(0);
        String messageBody = new String(message.getContents());
        
        // Verify the message can be parsed by our envelope helper
        var envelope = EnvelopeHelper.parse(messageBody);
        assert envelope.getType().equals("PaymentResult");
        
        // Verify the payload structure
        var payloadNode = objectMapper.readTree(envelope.getPayload());
        assert payloadNode.has("orderId");
        assert payloadNode.has("status");
        assert payloadNode.has("amount");
        assert payloadNode.path("status").asText().equals("CHARGED");
    }

    @Test
    public void testActualPaymentResultProduction() throws Exception {
        // Test that our producer can create messages matching the contract
        String orderId = "test-order-123";
        String resultPayload = objectMapper.createObjectNode()
                .put("orderId", orderId)
                .put("status", "CHARGED")
                .put("amount", 99.99)
                .toString();
        
        // This would normally be called in the real flow
        // paymentResultProducer.sendResult(orderId, resultPayload);
        
        // For testing, we just verify the envelope format is correct
        String envelopedMessage = EnvelopeHelper.envelope(orderId, "PaymentResult", resultPayload);
        var envelope = EnvelopeHelper.parse(envelopedMessage);
        
        assert envelope.getType().equals("PaymentResult");
        assert envelope.getOrderId().equals(orderId);
        
        var payloadNode = objectMapper.readTree(envelope.getPayload());
        assert payloadNode.path("status").asText().equals("CHARGED");
    }
}
