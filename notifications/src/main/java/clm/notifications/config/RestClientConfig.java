package clm.notifications.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configures a RestClient pre-wired with the contracts microservice base URL.
 */
@Configuration
public class RestClientConfig {

    @Value("${contracts.api.base-url}")
    private String contractsBaseUrl;

    @Bean
    public RestClient contractsRestClient() {
        return RestClient.builder()
                .baseUrl(contractsBaseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
