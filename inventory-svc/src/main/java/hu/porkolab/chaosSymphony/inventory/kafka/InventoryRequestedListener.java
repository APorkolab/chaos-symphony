package hu.porkolab.chaosSymphony.inventory.kafka;

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
public class InventoryRequestedListener {

    private final InventoryResultProducer producer;
    private final IdempotencyStore idempotencyStore;
    private final Counter messagesProcessed;
    private final Timer processingTime;
    private final ObjectMapper om = new ObjectMapper();

    @KafkaListener(topics = "${spring.kafka.topic.inventory.requested}", groupId = "${spring.kafka.group.id.inventory}")
    @Transactional
    public void onInventoryRequested(ConsumerRecord<String, String> rec) throws Exception {
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
            int items = msg.path("items").asInt(1);

            // Simulate inventory processing failure
            boolean success = items > 0 && ThreadLocalRandom.current().nextDouble() < 0.95;
            if (!success) {
                throw new IllegalStateException("Inventory processing failed for order: " + orderId);
            }

            String status = "RESERVED";
            String resultPayload = om.createObjectNode()
                    .put("orderId", orderId)
                    .put("status", status)
                    .put("items", items)
                    .toString();

            log.info("Inventory processed for orderId={}, status: {}", orderId, status);
            producer.sendResult(orderId, resultPayload);
        } finally {
            processingTime.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
