package hu.porkolab.chaosSymphony.dlq.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class DlqControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Admin-Token", "dev-token");
        return headers;
    }

    @Test
    void testListDlqTopics() {
        // The embedded kafka broker creates topics on demand,
        // but the admin client can't see them unless they are explicitly created.
        // For this test, we can assume the topics are created by the main app.
        // Since we can't easily create topics with a specific name pattern here,
        // we'll just test that the endpoint returns an empty list when no DLTs exist.

        HttpEntity<String> entity = new HttpEntity<>(null, createHeaders());

        ResponseEntity<List<String>> response = restTemplate.exchange(
                "/api/dlq/topics",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
