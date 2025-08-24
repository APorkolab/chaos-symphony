package hu.porkolab.chaosSymphony.dlq.api;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dlq")
public class DlqController {

	private final KafkaTemplate<String, String> template;
	private final Properties baseProps;

	public DlqController(KafkaTemplate<String, String> template,
			org.springframework.kafka.core.ProducerFactory<String, String> pf) {
		this.template = template;
		this.baseProps = new Properties();
		// bootstrap-servers-t a Spring configból veszi automatikusan a template/pf
		// Consumerhez külön kell:
		baseProps.putAll(pf.getConfigurationProperties());
		baseProps.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-admin-" + UUID.randomUUID());
		baseProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		baseProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		baseProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		// producer serializer (replay-hez)
		baseProps.put("key.serializer", StringSerializer.class.getName());
		baseProps.put("value.serializer", StringSerializer.class.getName());
	}

	@GetMapping("/topics")
	public List<String> listDlqTopics() throws Exception {
		try (var admin = AdminClient.create(baseProps)) {
			var names = admin.listTopics(new ListTopicsOptions().listInternal(false))
					.names().get();
			return names.stream()
					.filter(n -> n.endsWith(".DLT"))
					.sorted()
					.collect(Collectors.toList());
		}
	}

	@PostMapping("/{topic}/replay")
	public ResponseEntity<String> replay(@PathVariable String topic) {
		if (!topic.endsWith(".DLT"))
			return ResponseEntity.badRequest().body("Not a DLT topic");
		String original = topic.substring(0, topic.length() - 4); // remove ".DLT"

		var consumerProps = new Properties();
		consumerProps.putAll(baseProps);
		try (var consumer = new KafkaConsumer<String, String>(consumerProps)) {
			consumer.subscribe(Collections.singletonList(topic));
			long replayed = 0;
			while (true) {
				var records = consumer.poll(Duration.ofMillis(300));
				if (records.isEmpty())
					break;
				for (var rec : records) {
					try {
						RecordMetadata md = template.send(original, rec.key(), rec.value())
								.get().getRecordMetadata();
						replayed++;
					} catch (Exception e) {
						// ha a replay során hiba van, megy tovább
					}
				}
			}
			return ResponseEntity.ok("Replayed " + replayed + " records from " + topic + " to " + original);
		}
	}

	@DeleteMapping("/{topic}")
	public ResponseEntity<String> purge(@PathVariable String topic) {
		// egyszerű purge: új consumer group-pal végigolvas és eldobja
		var consumerProps = new Properties();
		consumerProps.putAll(baseProps);
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-purge-" + UUID.randomUUID());
		try (var consumer = new KafkaConsumer<String, String>(consumerProps)) {
			consumer.subscribe(Collections.singletonList(topic));
			long purged = 0;
			while (true) {
				var records = consumer.poll(Duration.ofMillis(300));
				if (records.isEmpty())
					break;
				purged += records.count();
			}
			return ResponseEntity.ok("Purged " + purged + " records from " + topic);
		}
	}
}
