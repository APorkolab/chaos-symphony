package hu.porkolab.chaosSymphony.common.chaos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class ChaosProducer {
    private static final Logger log = LoggerFactory.getLogger(ChaosProducer.class);
    private final KafkaTemplate<String,String> kafka;
    private final ChaosRules rules;

    public ChaosProducer(KafkaTemplate<String,String> kafka, ChaosRules rules) {
        this.kafka = kafka; this.rules = rules;
    }

    public void send(String topic, String key, String type, String msg) {
        var r = rules.ruleFor(topic, type);
        rules.maybeDelay(r.maxDelayMs());
        if (rules.hit(r.pDrop())) { log.warn("[CHAOS] DROP topic={} key={} type={}", topic, key, type); return; }
        if (rules.hit(r.pCorrupt())) {
            int cut = Math.max(1, msg.length()/2);
            msg = msg.substring(0, cut);
            log.warn("[CHAOS] CORRUPT topic={} key={} type={} cut={}", topic, key, type, cut);
        }
        kafka.send(topic, key, msg);
        if (rules.hit(r.pDup())) {
            kafka.send(topic, key, msg);
            log.warn("[CHAOS] DUP topic={} key={} type={}", topic, key, type);
        }
    }
}
