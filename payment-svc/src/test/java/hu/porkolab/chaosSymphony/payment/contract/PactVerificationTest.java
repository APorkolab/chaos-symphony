package hu.porkolab.chaosSymphony.payment.contract;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import hu.porkolab.chaosSymphony.payment.store.PaymentStatusStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("PaymentSvc")
@PactFolder("../../../orchestrator/target/pacts")
public class PactVerificationTest {

    @Autowired
    private PaymentStatusStore paymentStatusStore;

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        // This will automatically run the verification against the running Spring Boot application
        context.verifyInteraction();
    }

    @BeforeEach
    void setUp(PactVerificationContext context) {
        // This is a hook to clear state before each interaction.
        // It's good practice to ensure tests are isolated.
        paymentStatusStore.clear();
    }

    @State("a payment status exists for an order")
    public void toPaymentStatusExistsState(Map<String, Object> params) {
        // This method is called by the Pact framework to set up the provider state.
        // The 'params' map contains the data passed from the consumer's 'given' clause.
        String orderId = (String) params.get("orderId");
        paymentStatusStore.save(orderId, "CHARGED");
        System.out.println("Provider state setup for 'a payment status exists for an order' with orderId: " + orderId);
    }
}