package clm.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NegotiationRestClientConfig {

    @Value("${negotiation.service.url}")
    private String negotiationServiceUrl;

    @Bean
    public RestClient negotiationRestClient() {
        return RestClient.builder()
                .baseUrl(negotiationServiceUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
