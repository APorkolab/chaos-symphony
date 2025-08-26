package hu.porkolab.chaosSymphony.orderapi.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateOrder(
    @NotNull @DecimalMin(value = "0.01") BigDecimal total, String customerId) {
  public CreateOrder(@NotNull @DecimalMin(value = "0.01") BigDecimal total) {
    this(total, null);
  }
}
