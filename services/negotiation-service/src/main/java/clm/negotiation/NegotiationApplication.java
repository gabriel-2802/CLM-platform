package clm.negotiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NegotiationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NegotiationApplication.class, args);
    }
}
