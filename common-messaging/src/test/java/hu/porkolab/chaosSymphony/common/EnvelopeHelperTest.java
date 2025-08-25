package hu.porkolab.chaosSymphony.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnvelopeHelperTest {

	@Test
	void roundtrip() throws Exception {
		String msg = EnvelopeHelper.envelope("OID", "PaymentRequested", "{\"x\":1}");
		EventEnvelope env = EnvelopeHelper.parse(msg);
		assertEquals("OID", env.getOrderId());
		assertEquals("PaymentRequested", env.getType());
		assertEquals("{\"x\":1}", env.getPayload());
	}
}
