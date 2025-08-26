package hu.porkolab.chaosSymphony.chaos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/canary")
@Slf4j
public class CanaryController {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ORCHESTRATOR_ENV_URL = "http://orchestrator:8091/actuator/env";

    public record CanaryConfig(boolean enabled, double percentage) {}

    @PostMapping("/config")
    public void configureCanary(@RequestBody CanaryConfig config) throws Exception {
        log.info("Setting canary mode to enabled={} with percentage={}", config.enabled(), config.percentage());

        double percentageToSet = config.enabled() ? config.percentage() : 0.0;

        Map<String, Object> body = Map.of("name", "canary.payment.percentage", "value", percentageToSet);
        String requestBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ORCHESTRATOR_ENV_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Failed to update orchestrator canary config. Status: {}, Body: {}", response.statusCode(), response.body());
            throw new RuntimeException("Failed to update orchestrator canary config.");
        }

        log.info("Orchestrator canary config updated successfully.");
    }
}
