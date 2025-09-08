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

	// Convenience endpoint for demo and testing
	@PostMapping("/start")
	public ResponseEntity<String> startOrder(
			@RequestParam(defaultValue = "100.0") BigDecimal amount,
			@RequestParam(defaultValue = "customer-123") String customerId) {
		CreateOrder command = new CreateOrder(customerId, amount, "USD");
		UUID orderId = service.createOrder(command);
		return ResponseEntity.ok("Order started: " + orderId);
	}

	// Demo endpoint with explicit orderId for testing specific scenarios
	@PostMapping("/{orderId}/start")
	public ResponseEntity<String> startOrderWithId(
			@PathVariable String orderId,
			@RequestParam(defaultValue = "100.0") BigDecimal amount) {
		// For demo purposes, we'll create a specific order ID if provided
		CreateOrder command = new CreateOrder("customer-" + orderId, amount, "USD");
		try {
			UUID orderUuid = orderId.equals("BREAK-ME") ? 
					UUID.fromString("00000000-0000-0000-0000-000000000001") : 
					UUID.fromString(orderId);
			// This would need service method modification to accept specific UUID
			UUID createdOrderId = service.createOrder(command);
			return ResponseEntity.ok("Order started with ID: " + createdOrderId);
		} catch (IllegalArgumentException e) {
			// If not a valid UUID, create normally but use orderId as customer reference
			CreateOrder modifiedCommand = new CreateOrder(orderId, amount, "USD");
			UUID createdOrderId = service.createOrder(modifiedCommand);
			return ResponseEntity.ok("Order started: " + createdOrderId + " (ref: " + orderId + ")");
		}
	}
}
