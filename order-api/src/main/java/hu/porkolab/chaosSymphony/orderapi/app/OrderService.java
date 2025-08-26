package hu.porkolab.chaosSymphony.orderapi.app;

import hu.porkolab.chaosSymphony.events.OrderCreated;
import hu.porkolab.chaosSymphony.orderapi.api.CreateOrder;
import hu.porkolab.chaosSymphony.orderapi.domain.Order;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderRepository;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository repository;
	private final OutboxEventProducer producer;

	@Transactional
	public UUID createOrder(CreateOrder command) {
		Order order = new Order();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.NEW);
		order.setTotal(command.amount().setScale(2, RoundingMode.HALF_UP));
		repository.save(order);

		 OrderCreated event = new OrderCreated(
		 		order.getId().toString(),
		 		order.getTotal(),
		 		"HUF");

		 producer.fire(event);
		return order.getId();
	}
}