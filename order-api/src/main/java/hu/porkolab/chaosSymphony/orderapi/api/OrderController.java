package hu.porkolab.chaosSymphony.orderapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.porkolab.chaosSymphony.orderapi.kafka.PaymentRequestProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final PaymentRequestProducer producer;
	private final ObjectMapper om = new ObjectMapper();

	public OrderController(PaymentRequestProducer producer) {
		this.producer = producer;
	}

	// REST endpoint: indítja az order folyamatot
	@PostMapping("/{orderId}/start")
	public ResponseEntity<String> startOrder(@PathVariable String orderId, @RequestParam double amount) {
		try {
			// payload (üzleti adat)
			String payload = om.createObjectNode()
					.put("orderId", orderId)
					.put("amount", amount)
					.toString();

			// payment.requested üzenet küldése
			producer.sendRequest(orderId, payload);

			return ResponseEntity.ok("Order " + orderId + " started.");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Error starting order: " + e.getMessage());
		}
	}

	// Extra: ha nincs orderId, generálunk
	@PostMapping("/start")
	public ResponseEntity<String> startNewOrder(@RequestParam double amount) {
		String orderId = UUID.randomUUID().toString();
		System.out.println("[ORDER-API] startOrder orderId=" + orderId + " amount=" + amount);
		return startOrder(orderId, amount);
	}
}
