package hu.porkolab.chaosSymphony.streams.api;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

	private final ReadOnlyKeyValueStore<String, Long> store;

	public MetricsController(KafkaStreams streams) {
		this.store = streams.store(
				StoreQueryParameters.fromNameAndType("counts-store", QueryableStoreTypes.keyValueStore()));
	}

	@GetMapping("/paymentStatus")
	public Map<String, Long> paymentStatus() {
		Map<String, Long> out = new LinkedHashMap<>();
		try (KeyValueIterator<String, Long> it = store.all()) {
			while (it.hasNext()) {
				var kv = it.next();
				out.put(kv.key, kv.value);
			}
		}
		return out;
	}
}
