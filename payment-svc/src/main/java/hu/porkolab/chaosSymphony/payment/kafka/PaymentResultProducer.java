package hu.porkolab.chaosSymphony.payment.kafka;

import hu.porkolab.chaosSymphony.common.EnvelopeHelper;
import hu.porkolab.chaosSymphony.common.chaos.ChaosProducer;
import hu.porkolab.chaosSymphony.common.chaos.ChaosRules;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultProducer {

	private final ChaosProducer chaos;

	public PaymentResultProducer(ChaosProducer chaos) {
		this.chaos = chaos;
	}

	public void sendResult(String orderId, String resultPayloadJson) {
		String msg = EnvelopeHelper.envelope(orderId, "PaymentResult", resultPayloadJson);
		chaos.send("payment.result", orderId, "PaymentResult", msg);
	}

	// Beanek ugyanebben a modulban (egyszerű fix, később kiteheted config
	// osztályba)
	@Bean
	public ChaosProducer chaosProducer(KafkaTemplate<String, String> tpl, ChaosRules rules) {
		return new ChaosProducer(tpl, rules);
	}
}
