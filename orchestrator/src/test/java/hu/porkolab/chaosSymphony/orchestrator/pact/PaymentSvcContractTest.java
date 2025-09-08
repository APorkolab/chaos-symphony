package hu.porkolab.chaosSymphony.orchestrator.pact;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import hu.porkolab.chaosSymphony.orchestrator.config.TestConfig;
import hu.porkolab.chaosSymphony.orchestrator.kafka.PaymentProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, 
    topics = {"payment.request", "payment.result"},
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@PactTestFor(providerName = "payment-svc", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
@ContextConfiguration(classes = TestConfig.class)
public class PaymentSvcContractTest {

    @MockBean
    private PaymentProducer paymentProducer;

    @Pact(consumer = "orchestrator")
    public MessagePact createPaymentRequestedPact(MessagePactBuilder builder) {
        return builder
                .expectsToReceive("A payment requested event")
                .withContent(newJsonBody(envelope -> {
                    envelope.stringType("orderId", "e7a4f431-b2e3-4b43-8a24-8e2b1d3a0e46");
                    envelope.stringType("eventId", "f8b5c2d1-3e4f-5a6b-7c8d-9e0f1a2b3c4d");
                    envelope.stringType("type", "PaymentRequested");
                    envelope.stringType("payload", "{\"orderId\":\"e7a4f431-b2e3-4b43-8a24-8e2b1d3a0e46\",\"amount\":123.45,\"currency\":\"USD\"}");
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPaymentRequestedPact")
    public void testPaymentRequested(List<Message> messages) {
        // This test method needs to exist for the Pact framework to discover the
        // @Pact method ("createPaymentRequestedPact").
        // The body can be empty because we are mocking the PaymentProducer.
        // The goal is just to generate the contract file, not to test the producer's logic.
        assert !messages.isEmpty();
    }
}
