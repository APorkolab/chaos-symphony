package hu.porkolab.chaosSymphony.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
public class StreamsAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamsAnalyticsApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public KafkaStreams kafkaStreams(Topology topology) {
        Properties p = new Properties();
        p.put("application.id", "streams-analytics");
        p.put("bootstrap.servers", "127.0.0.1:29092");
        p.put("auto.offset.reset", "earliest");
        KafkaStreams streams = new KafkaStreams(topology, p);
        streams.start();
        return streams;
    }
}
