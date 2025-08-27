package hu.porkolab.chaosSymphony.orderapi.api;

import hu.porkolab.chaosSymphony.orderapi.app.OrderService;
import hu.porkolab.chaosSymphony.orderapi.domain.Order;
import hu.porkolab.chaosSymphony.orderapi.domain.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService service;
	private final OrderRepository repository;

	@PostMapping
	public ResponseEntity<UUID> createOrder(@RequestBody @Valid CreateOrder command) {
		UUID orderId = service.createOrder(command);
		return ResponseEntity.accepted().body(orderId);
	}

	@GetMapping
	public List<Order> getAllOrders() {
		// Return latest orders first
		return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
		return repository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
