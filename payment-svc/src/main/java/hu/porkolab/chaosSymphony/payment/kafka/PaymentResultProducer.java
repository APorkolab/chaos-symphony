package hu.porkolab.chaosSymphony.payment.kafka;

import hu.porkolab.chaosSymphony.common.chaos.ChaosProducer;
import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultProducer {
    private static final Logger log = LoggerFactory.getLogger(PaymentResultProducer.class);
    private final ChaosProducer chaos;

    public PaymentResultProducer(ChaosProducer chaos) {
        this.chaos = chaos;
    }

    public void sendResult(String orderId, String payloadJson) {
        String msg = EnvelopeHelper.envelope(orderId, "PaymentResult", payloadJson);
        chaos.send("payment.result", orderId, "PaymentResult", msg);
        log.info("[PAYMENT] queued payment.result key={}", orderId);
    }
}
