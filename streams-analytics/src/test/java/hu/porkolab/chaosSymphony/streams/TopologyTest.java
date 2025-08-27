package hu.porkolab.chaosSymphony.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Topology topology = new TopologyConfig().topology();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic("payment.result", Serdes.String().serializer(), Serdes.String().serializer());
        outputTopic = testDriver.createOutputTopic("analytics.payment.status.count", Serdes.String().deserializer(), Serdes.Long().deserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldCountPaymentStatuses() {
        // Given
        inputTopic.pipeInput("order1", createPaymentResultMessage("CHARGED"));
        inputTopic.pipeInput("order2", createPaymentResultMessage("CHARGED"));
        inputTopic.pipeInput("order3", createPaymentResultMessage("FAILED"));

        // Then
        assertEquals(new KeyValue<>("CHARGED", 1L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("CHARGED", 2L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("FAILED", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void shouldHandleMalformedJsonAsUnknown() {
        // Given
        inputTopic.pipeInput("malformed1", "this is not json");

        // Then
        assertEquals(new KeyValue<>("UNKNOWN", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void shouldHandleMissingStatusAsUnknown() {
        // Given
        inputTopic.pipeInput("missingStatus1", createPaymentResultMessage(null));

        // Then
        assertEquals(new KeyValue<>("UNKNOWN", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());
    }

    private String createPaymentResultMessage(String status) {
        try {
            ObjectNode payload = MAPPER.createObjectNode();
            if (status != null) {
                payload.put("status", status);
            }

            ObjectNode root = MAPPER.createObjectNode();
            root.put("id", "some-id");
            // The topology expects the payload to be a string, which is then parsed again.
            // This matches the "double json" format handled by the production code.
            root.put("payload", payload.toString());
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
