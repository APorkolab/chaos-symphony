package hu.porkolab.chaosSymphony.payment.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.payment.outbox.IdempotentOutbox;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Eredmény küldése: payment.result
 * Idempotencia: egyszerű outbox cache (same orderId+payload -> csak egyszer
 * küldi).
 */
@Component
public class PaymentResultProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultProducer.class);

    private final KafkaTemplate<String, String> kafka;
    private final IdempotentOutbox outbox;

    public PaymentResultProducer(KafkaTemplate<String, String> kafka, IdempotentOutbox outbox) {
        this.kafka = kafka;
        this.outbox = outbox;
    }

    public void sendResult(String orderId, String resultPayloadJson) {
        String eventId = UUID.randomUUID().toString();
        String outKey = orderId + "|" + Integer.toHexString(resultPayloadJson.hashCode());

        if (!outbox.markIfFirst(outKey)) {
            log.debug("[PAYMENT] duplicate result suppressed key={}", outKey);
            return;
        }

        try {
            String msg = EnvelopeHelper.envelope(orderId, eventId, "PaymentResult", resultPayloadJson);
            RecordMetadata md = kafka.send("payment.result", orderId, msg).get().getRecordMetadata();
            log.info("[PAYMENT] → payment.result key={} {}-{}@{}", orderId, md.topic(), md.partition(), md.offset());
        } catch (Exception e) {
            log.error("[PAYMENT] send payment.result failed key={} err={}", orderId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
