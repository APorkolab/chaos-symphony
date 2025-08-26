package hu.porkolab.chaosSymphony.orchestrator.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import hu.porkolab.chaosSymphony.events.OrderCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {
    private final PaymentProducer producer;
    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper om = new ObjectMapper();

    @KafkaListener(topics = "order.created", groupId = "orchestrator-order-created")
    @Transactional
    public void onOrderCreated(ConsumerRecord<String, OrderCreated> rec) throws Exception {
        if (!idempotencyStore.markIfFirst(rec.key())) {
            log.warn("Duplicate message detected, skipping: {}", rec.key());
            return;
        }

        OrderCreated event = rec.value();
        String orderId = event.getOrderId().toString();
        String customerId = event.getCustomerId() == null ? "N/A" : event.getCustomerId().toString();

        log.info("OrderCreated received for orderId={}, customerId={} -> sending PaymentRequested", orderId, customerId);

        String paymentPayload = om.createObjectNode()
                .put("orderId", orderId)
                .put("amount", event.getTotal())
                .put("currency", event.getCurrency().toString())
                .toString();

        producer.sendPaymentRequested(orderId, paymentPayload);
    }
}