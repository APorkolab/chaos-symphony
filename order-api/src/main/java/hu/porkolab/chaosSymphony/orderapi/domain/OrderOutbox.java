package hu.porkolab.chaosSymphony.orderapi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOutbox {
	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID aggregateId; // orderId

	@Column(nullable = false)
	private String type; // OrderCreated

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload; // JSON

	@Column(nullable = false)
	private Instant occurredAt;

	@Column(nullable = false)
	private Boolean published;
}
