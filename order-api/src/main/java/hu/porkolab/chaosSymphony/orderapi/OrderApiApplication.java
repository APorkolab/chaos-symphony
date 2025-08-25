package hu.porkolab.chaosSymphony.orderapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@SpringBootApplication
@Import(hu.porkolab.chaosSymphony.common.kafka.KafkaErrorHandlingConfig.class)
public class OrderApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(OrderApiApplication.class, args);
	}

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
