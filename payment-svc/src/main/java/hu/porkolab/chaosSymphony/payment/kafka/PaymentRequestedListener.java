package hu.porkolab.chaosSymphony.payment.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.EventEnvelope;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import hu.porkolab.chaosSymphony.payment.store.PaymentStatusStore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.SocketTimeoutException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestedListener {

    private final PaymentResultProducer producer;
    private final IdempotencyStore idempotencyStore;
    private final PaymentStatusStore paymentStatusStore;
    private final Counter paymentsProcessedMain;
    private final Counter paymentsProcessedCanary;
    private final Timer processingTime;
    private final ObjectMapper om = new ObjectMapper();

    @RetryableTopic(
            attempts = "4", // 1 initial try + 3 retries
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true),
            include = {
                    SocketTimeoutException.class,
                    IllegalStateException.class
            },
            autoCreateTopics = "false" // In production, topics should be created by Ops/Terraform
    )
    @KafkaListener(topics = "${kafka.topic.payment.requested}", groupId = "${kafka.group.id.payment}")
    @Transactional
    public void onPaymentRequested(ConsumerRecord<String, String> rec) throws Exception {
        long startTime = System.nanoTime();
        try {
            paymentsProcessedMain.increment();
            if (!idempotencyStore.markIfFirst(rec.key())) {
                log.warn("Duplicate message detected, skipping: {}", rec.key());
                return;
            }

            EventEnvelope env = EnvelopeHelper.parse(rec.value());
            String orderId = env.getOrderId();

            JsonNode msg = om.readTree(env.getPayload());
            double amount = msg.path("amount").asDouble();

            // Simulate payment processing
            boolean success = ThreadLocalRandom.current().nextDouble() < 0.9;
            if (!success) {
                throw new IllegalStateException("Payment processing failed for order: " + orderId);
            }

            String status = "CHARGED";
            paymentStatusStore.save(orderId, status);

            String resultPayload = om.createObjectNode()
                    .put("orderId", orderId)
                    .put("status", status)
                    .put("amount", amount)
                    .toString();

            log.info("Payment processed for orderId={}, status: {}", orderId, status);
            producer.sendResult(orderId, resultPayload);
        } finally {
            processingTime.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
        ;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true),
            include = {
                    SocketTimeoutException.class,
                    IllegalStateException.class
            },
            autoCreateTopics = "false"
    )
    @KafkaListener(topics = "${kafka.topic.payment.requested.canary}", groupId = "${kafka.group.id.payment.canary}")
    @Transactional
    public void onPaymentRequestedCanary(ConsumerRecord<String, String> rec) throws Exception {
        long startTime = System.nanoTime();
        try {
            paymentsProcessedCanary.increment();
            log.info("[CANARY] Received payment request for key: {}", rec.key());
            if (!idempotencyStore.markIfFirst(rec.key())) {
                log.warn("[CANARY] Duplicate message detected, skipping: {}", rec.key());
                return;
            }

            EventEnvelope env = EnvelopeHelper.parse(rec.value());
            String orderId = env.getOrderId();

            JsonNode msg = om.readTree(env.getPayload());
            double amount = msg.path("amount").asDouble();

            // Simulate payment processing
            boolean success = ThreadLocalRandom.current().nextDouble() < 0.9;
            if (!success) {
                throw new IllegalStateException("[CANARY] Payment processing failed for order: " + orderId);
            }

            String status = "CHARGED";
            paymentStatusStore.save(orderId, status);

            String resultPayload = om.createObjectNode()
                    .put("orderId", orderId)
                    .put("status", status)
                    .put("amount", amount)
                    .toString();

            log.info("[CANARY] Payment processed for orderId={}, status: {}", orderId, status);
            producer.sendResult(orderId, resultPayload);
        } finally {
            processingTime.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
