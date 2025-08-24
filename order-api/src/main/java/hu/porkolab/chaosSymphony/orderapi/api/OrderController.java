package hu.porkolab.chaosSymphony.orderapi.api;

import hu.porkolab.chaosSymphony.orderapi.app.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
	private final OrderService service;

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody @Validated CreateOrder cmd) {
		UUID id = service.createOrder(cmd);
		return ResponseEntity.ok(Map.of("orderId", id.toString(), "status", "NEW"));
	}
}
