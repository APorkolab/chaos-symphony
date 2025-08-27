package hu.porkolab.chaosSymphony.chaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ChaosSvcApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChaosSvcApplication.class, args);
    }
}
