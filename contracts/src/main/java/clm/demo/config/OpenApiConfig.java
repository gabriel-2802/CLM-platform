package clm.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Global OpenAPI / Swagger UI configuration.
 *
 * <p>Accessible at:
 * <ul>
 *   <li>Swagger UI  — {@code /swagger-ui.html}</li>
 *   <li>Raw spec    — {@code /v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CLM Platform API")
                        .description("""
                                Contract Lifecycle Management (CLM) Platform — REST API.

                                Manages the full document lifecycle:
                                - **Templates** — upload DOCX/PDF blueprints and map placeholder fields
                                - **Contracts** — generate, sign, terminate, search and download contracts
                                - **Appendices** — generate or directly upload auxiliary documents per contract
                                - **Reports** — expiry alerts and inactive-client digests for the notification service
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CLM Platform Team")
                                .email("support@clm-platform.com")))
                .tags(List.of(
                        new Tag().name("Templates")
                                 .description("Upload and manage document templates with placeholder fields"),
                        new Tag().name("Contracts")
                                 .description("Generate, sign, terminate, search, and download contracts"),
                        new Tag().name("Appendices")
                                 .description("Generate, upload, sign, and download contract appendices"),
                        new Tag().name("Reports")
                                 .description("Reporting endpoints consumed by the notification microservice")
                ));
    }
}
