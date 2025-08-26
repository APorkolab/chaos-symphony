package hu.porkolab.chaosSymphony.orchestrator.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "PaymentSvc", pactVersion = PactSpecVersion.V4)
public class PaymentSvcContractTest {

    private static final String ORDER_ID = "a7e7c61b-9a89-424f-a496-3c6a02a1d478";

    @Pact(consumer = "Orchestrator")
    public V4Pact paymentStatusExists(PactDslWithProvider builder) {
        return builder
            .given("a payment status exists for order " + ORDER_ID)
            .uponReceiving("a request for payment status")
                .method("GET")
                .path("/api/payments/status/" + ORDER_ID)
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body("{\"orderId\": \"" + ORDER_ID + "\", \"status\": \"CHARGED\"}")
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "paymentStatusExists")
    void testPaymentStatusExists(MockServer mockServer) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mockServer.getUrl() + "/api/payments/status/" + ORDER_ID))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }
}
