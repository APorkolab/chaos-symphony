package hu.porkolab.chaosSymphony.events;

import java.math.BigDecimal;

public record OrderCreated(
		String orderId,
		BigDecimal total,
		String currency) {
}