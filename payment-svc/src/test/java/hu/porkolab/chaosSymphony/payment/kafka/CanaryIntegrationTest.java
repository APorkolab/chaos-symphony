package hu.porkolab.chaosSymphony.payment.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "kafka.topic.payment.requested=payment.requested",
                "kafka.topic.payment.requested.canary=payment.requested.canary",
                "kafka.group.id.payment=payment-group",
                "kafka.group.id.payment.canary=payment-canary-group"
        }
)
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = { "payment.requested", "payment.requested.canary" })
class CanaryIntegrationTest {

    @Autowired
    private Counter paymentsProcessedMain;

    @Autowired
    private Counter paymentsProcessedCanary;

    @Value("${kafka.topic.payment.requested}")
    private String mainTopic;

    @Value("${kafka.topic.payment.requested.canary}")
    private String canaryTopic;

    @Autowired
    private org.springframework.kafka.core.ProducerFactory<String, String> pf;

    @Test
    void testMainAndCanaryConsumers() {
        // Given
        double initialMainCount = paymentsProcessedMain.count();
        double initialCanaryCount = paymentsProcessedCanary.count();

        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);

        // When we send a message to the main topic
        String mainPayload = "{\"orderId\":\"order1\",\"amount\":100.0,\"currency\":\"USD\"}";
        String mainEnvelope = EnvelopeHelper.envelope("order1", "PaymentRequested", mainPayload);
        template.send(mainTopic, "order1", mainEnvelope);

        // Then the main counter should increment
        await().atMost(5, SECONDS).untilAsserted(() ->
            assertEquals(initialMainCount + 1, paymentsProcessedMain.count())
        );

        // When we send a message to the canary topic
        String canaryPayload = "{\"orderId\":\"order2\",\"amount\":200.0,\"currency\":\"USD\"}";
        String canaryEnvelope = EnvelopeHelper.envelope("order2", "PaymentRequested", canaryPayload);
        template.send(canaryTopic, "order2", canaryEnvelope);

        // Then the canary counter should increment
        await().atMost(5, SECONDS).untilAsserted(() ->
            assertEquals(initialCanaryCount + 1, paymentsProcessedCanary.count())
        );
    }
}
