package hu.porkolab.chaosSymphony.orderapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.orderapi.kafka.PaymentRequestProducer;
import io.micrometer.core.instrument.Counter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final PaymentRequestProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	// Micrometer Counters
	private final Counter ordersStarted;

	public OrderController(PaymentRequestProducer producer,
			Counter ordersStarted) {
		this.producer = producer;
		this.ordersStarted = ordersStarted;
	}

	@PostMapping("/{orderId}/start")
	public ResponseEntity<String> startOrder(@PathVariable String orderId, @RequestParam double amount) {
		try {
			ordersStarted.increment();

			String payload = om.createObjectNode()
					.put("orderId", orderId)
					.put("amount", amount)
					.toString();

			producer.sendRequest(orderId, payload);
			return ResponseEntity.ok("Order " + orderId + " started.");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Error starting order: " + e.getMessage());
		}
	}

	@PostMapping("/start")
	public ResponseEntity<String> startNewOrder(@RequestParam double amount) {
		String orderId = UUID.randomUUID().toString();
		return startOrder(orderId, amount);
	}
}
