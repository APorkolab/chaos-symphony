package hu.porkolab.chaosSymphony.payment.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter paymentsProcessedMain(MeterRegistry registry) {
        return Counter.builder("payments.processed")
                .tag("channel", "main")
                .description("The number of payment requests processed by the main consumer.")
                .register(registry);
    }

    @Bean
    public Counter paymentsProcessedCanary(MeterRegistry registry) {
        return Counter.builder("payments.processed")
                .tag("channel", "canary")
                .description("The number of payment requests processed by the canary consumer.")
                .register(registry);
    }
}
