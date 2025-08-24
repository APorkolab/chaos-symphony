package hu.porkolab.chaosSymphony.orderapi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
	@Id
	private UUID id;

	@Column(nullable = false)
	private String status; // NEW|PAID|ALLOCATED|SHIPPED|FAILED

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(nullable = false)
	private Instant createdAt;
}
