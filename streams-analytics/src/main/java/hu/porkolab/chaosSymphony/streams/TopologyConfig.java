package hu.porkolab.chaosSymphony.streams;

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

	@Bean
	public Topology topology() {
		StreamsBuilder b = new StreamsBuilder();

		KStream<String, String> payments = b.stream("payment.result", Consumed.with(Serdes.String(), Serdes.String()));

		Materialized<String, Long, KeyValueStore<Bytes, byte[]>> mat = Materialized
				.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store")
				.withKeySerde(Serdes.String())
				.withValueSerde(Serdes.Long());

		KTable<String, Long> byStatus = payments
				.mapValues(v -> extract(v, "\"status\":\"", "\""))
				.groupBy((k, status) -> status, Grouped.with(Serdes.String(), Serdes.String()))
				.count(mat);

		byStatus.toStream()
				.to("analytics.payment.status.count", Produced.with(Serdes.String(), Serdes.Long()));

		return b.build();
	}

	private static String extract(String s, String prefix, String endQuote) {
		int i = s.indexOf(prefix);
		if (i < 0)
			return "UNKNOWN";
		i += prefix.length();
		int j = s.indexOf(endQuote, i);
		return j < 0 ? "UNKNOWN" : s.substring(i, j);
	}
}
