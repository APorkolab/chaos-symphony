package hu.porkolab.chaosSymphony.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopologyConfig {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Bean
	public Topology topology() {
		StreamsBuilder b = new StreamsBuilder();

		KStream<String, String> payments = b.stream("payment.result", Consumed.with(Serdes.String(), Serdes.String()));

		Materialized<String, Long, KeyValueStore<Bytes, byte[]>> mat = Materialized
				.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store")
				.withKeySerde(Serdes.String())
				.withValueSerde(Serdes.Long());

		KTable<String, Long> byStatus = payments
				.mapValues(TopologyConfig::statusFromEnvelope)
				.groupBy((k, status) -> status, Grouped.with(Serdes.String(), Serdes.String()))
				.count(mat);

		byStatus.toStream()
				.to("analytics.payment.status.count", Produced.with(Serdes.String(), Serdes.Long()));

		return b.build();
	}

	private static String statusFromEnvelope(String json) {
		try {
			JsonNode root = MAPPER.readTree(json);
			JsonNode payloadNode = root.path("payload");

			// a payload lehet TEXT (escape-elt JSON) vagy objektum
			String payloadStr = payloadNode.isTextual() ? payloadNode.asText() : payloadNode.toString();
			JsonNode payload = MAPPER.readTree(payloadStr);

			return payload.path("status").asText("UNKNOWN");
		} catch (Exception e) {
			return "UNKNOWN";
		}
	}
}
