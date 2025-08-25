package hu.porkolab.chaosSymphony.payment.contract;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("PaymentSvc")
@PactFolder("../../../orchestrator/target/pacts")
public class PactVerificationTest {

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        // This will automatically run the verification against the running Spring Boot application
        context.verifyInteraction();
    }

    @State("a payment status exists for an order")
    public void toPaymentStatusExistsState() {
        // No specific state setup is needed for this simple case,
        // as the controller returns a hardcoded response.
        System.out.println("Provider state 'a payment status exists for an order' is active. No setup needed.");
    }
}