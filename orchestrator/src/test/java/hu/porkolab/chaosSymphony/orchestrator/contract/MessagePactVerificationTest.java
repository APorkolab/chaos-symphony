package hu.porkolab.chaosSymphony.orchestrator.contract;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, 
    topics = {"payment.result", "inventory.request", "shipping.request", "notification.request"},
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Provider("payment-result-producer")
@PactFolder("target/pacts")
public class MessagePactVerificationTest {

    private static final String ORDER_ID = UUID.randomUUID().toString();
    private final ObjectMapper om = new ObjectMapper();

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @PactVerifyProvider("A payment result event")
    public MessageAndMetadata verifyPaymentResultEvent() {
        String resultPayload = om.createObjectNode()
                .put("orderId", ORDER_ID)
                .put("status", "CHARGED")
                .put("amount", 123.45)
                .toString();
        String envelopedMessage = EnvelopeHelper.envelope(ORDER_ID, "PaymentResult", resultPayload);
        return new MessageAndMetadata(envelopedMessage.getBytes(), Map.of());
    }
}
