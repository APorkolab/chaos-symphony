package hu.porkolab.chaosSymphony.orchestrator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
	@Bean
	public Counter ordersStarted(MeterRegistry reg) {
		return Counter.builder("orders.started").register(reg);
	}

	@Bean
	public Counter ordersSucceeded(MeterRegistry reg) {
		return Counter.builder("orders.succeeded").register(reg);
	}

	@Bean
	public Counter ordersFailed(MeterRegistry reg) {
		return Counter.builder("orders.failed").register(reg);
	}
}
