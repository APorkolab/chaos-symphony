package hu.porkolab.chaosSymphony.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = "hu.porkolab.chaosSymphony")
public class PaymentSvcApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentSvcApplication.class, args);
    }
}
