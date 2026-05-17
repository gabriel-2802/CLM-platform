package clm.negotiation.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name         = "bearerAuth",
        type         = SecuritySchemeType.HTTP,
        scheme       = "bearer",
        bearerFormat = "JWT",
        in           = SecuritySchemeIn.HEADER,
        description  = "Paste the JWT issued by the auth service (without the 'Bearer ' prefix)."
)
public class OpenApiConfig {

    @Bean
    public OpenAPI negotiationOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("https://localhost")
                        .description("CLM Platform — via Nginx"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("CLM Negotiation Service API")
                        .description("""
                                Manages contract renegotiation rounds.

                                - **Create** a DRAFT negotiation with proposed price and/or end date
                                - **Send** the proposal to the client (DRAFT → SENT)
                                - **Accept** the proposal — automatically updates the contract (SENT → ACCEPTED)
                                - **Reject** the proposal with an optional explanation (SENT → REJECTED)
                                - **Notes** can be updated at any stage to record context or rejection reasons
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CLM Platform Team")
                                .email("support@clm-platform.com")))
                .tags(List.of(
                        new Tag().name("Negotiations")
                                 .description("Manage contract renegotiation rounds")
                ));
    }
}
