package hu.porkolab.chaosSymphony.orderapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "order_outbox")
public class OutboxEvent {
	@Id
	@GeneratedValue
	private UUID id;
	private String aggregateType;
	private String aggregateId;
	private String type;
	@Column(columnDefinition = "text")
	private String payload;
	@CreationTimestamp
	private Instant createdAt;
}