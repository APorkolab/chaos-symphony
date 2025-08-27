package hu.porkolab.chaosSymphony.orderapi.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record CreateOrder(
    @NotNull @DecimalMin(value = "0.01") BigDecimal total,
    @NotNull @NotBlank String customerId
) {
}
