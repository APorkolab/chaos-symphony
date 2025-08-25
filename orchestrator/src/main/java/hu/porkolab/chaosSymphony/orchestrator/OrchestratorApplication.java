package hu.porkolab.chaosSymphony.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.context.annotation.Import(hu.porkolab.chaosSymphony.common.kafka.KafkaErrorHandlingConfig.class)
public class OrchestratorApplication {
	public static void main(String[] args) {
		SpringApplication.run(OrchestratorApplication.class, args);
	}
}
