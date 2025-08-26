package hu.porkolab.chaosSymphony.payment.contract;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import hu.porkolab.chaosSymphony.payment.store.PaymentStatusStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("PaymentSvc")
@PactFolder("../../../orchestrator/target/pacts")
@Testcontainers // 1. LÉPÉS: Engedélyezzük a Testcontainers használatát
public class PactVerificationTest {

    // 2. LÉPÉS: Definiálunk egy PostgreSQL konténert, ami a teszt előtt elindul
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // 3. LÉPÉS: Dinamikusan beállítjuk a Spring-et, hogy ehhez a konténerhez
    // csatlakozzon
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // A Spring Boot automatikusan lefuttatja a schema.sql-t ezen az adatbázison
    }

    @Autowired
    private PaymentStatusStore paymentStatusStore;

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void setUp(PactVerificationContext context) {
        paymentStatusStore.clear();
    }

    @State("a payment status exists for an order")
    public void toPaymentStatusExistsState(Map<String, Object> params) {
        String orderId = (String) params.get("orderId");
        paymentStatusStore.save(orderId, "CHARGED");
        System.out.println("Provider state setup for 'a payment status exists for an order' with orderId: " + orderId);
    }
}