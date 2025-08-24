package hu.porkolab.chaosSymphony.streams.api;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

	private final ReadOnlyKeyValueStore<String, Long> store;

	public MetricsController(org.apache.kafka.streams.KafkaStreams streams) {
		this.store = streams.store(StoreQueryParameters.fromNameAndType(
				"counts-store", QueryableStoreTypes.keyValueStore()));
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
