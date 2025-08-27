package hu.porkolab.chaosSymphony.payment.kafka;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
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
        template.send(mainTopic, "order1", "{\"message\":\"to-main\"}");

        // Then the main counter should increment
        await().atMost(5, SECONDS).untilAsserted(() ->
            assertEquals(initialMainCount + 1, paymentsProcessedMain.count())
        );

        // When we send a message to the canary topic
        template.send(canaryTopic, "order2", "{\"message\":\"to-canary\"}");

        // Then the canary counter should increment
        await().atMost(5, SECONDS).untilAsserted(() ->
            assertEquals(initialCanaryCount + 1, paymentsProcessedCanary.count())
        );
    }
}
