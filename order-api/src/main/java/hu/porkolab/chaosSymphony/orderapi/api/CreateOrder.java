package hu.porkolab.chaosSymphony.orderapi.api;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateOrder(
		@NotNull @DecimalMin("0.01") BigDecimal amount) {
}