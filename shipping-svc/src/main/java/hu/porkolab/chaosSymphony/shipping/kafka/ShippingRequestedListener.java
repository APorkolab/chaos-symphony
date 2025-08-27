package hu.porkolab.chaosSymphony.shipping.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingRequestedListener {

    private final ShippingResultProducer producer;
    private final IdempotencyStore idempotencyStore;
    private final Counter messagesProcessed;
    private final Timer processingTime;
    private final ObjectMapper om = new ObjectMapper();

    @KafkaListener(topics = "${spring.kafka.topic.shipping.requested}", groupId = "${spring.kafka.group.id.shipping}")
    @Transactional
    public void onShippingRequested(ConsumerRecord<String, String> rec) throws Exception {
        long startTime = System.nanoTime();
        try {
            messagesProcessed.increment();
            if (!idempotencyStore.markIfFirst(rec.key())) {
                log.warn("Duplicate message detected, skipping: {}", rec.key());
                return;
            }

            EventEnvelope env = EnvelopeHelper.parse(rec.value());
            String orderId = env.getOrderId();

            JsonNode msg = om.readTree(env.getPayload());
            String address = msg.path("address").asText("UNKNOWN");

            // Simulate shipping processing failure
            boolean success = !"UNKNOWN".equals(address) && ThreadLocalRandom.current().nextDouble() < 0.98;
            if (!success) {
                throw new IllegalStateException("Shipping processing failed for order: " + orderId);
            }

            String status = "DELIVERED";
            String resultPayload = om.createObjectNode()
                    .put("orderId", orderId)
                    .put("status", status)
                    .put("address", address)
                    .toString();

            log.info("Shipping processed for orderId={}, status: {}", orderId, status);
            producer.sendResult(orderId, resultPayload);
        } finally {
            processingTime.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
