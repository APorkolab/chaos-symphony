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

import java.util.Map;
import java.util.UUID;

@Provider("Orchestrator")
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

    @PactVerifyProvider("an inventory requested event")
    public MessageAndMetadata verifyInventoryRequestedEvent() {
        ObjectNode payload = om.createObjectNode().put("orderId", ORDER_ID);
        String envelopedMessage = EnvelopeHelper.envelope(ORDER_ID, "InventoryRequested", payload.toString());
        return new MessageAndMetadata(envelopedMessage.getBytes(), Map.of());
    }
}
