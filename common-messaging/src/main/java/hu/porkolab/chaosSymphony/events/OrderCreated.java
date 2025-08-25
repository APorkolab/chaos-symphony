package hu.porkolab.chaosSymphony.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ez a végleges "szerződés". Az esemény, amit a rendszer kibocsát, amikor egy rendelés létrejön.
 */
public record OrderCreated(
		UUID orderId,
		BigDecimal total,
		String currency) {
}