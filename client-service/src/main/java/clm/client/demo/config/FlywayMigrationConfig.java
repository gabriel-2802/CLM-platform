package clm.client.demo.config;

import clm.client.demo.exceptions.DatabaseValidationException;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class FlywayMigrationConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .validateOnMigrate(true)
                    .outOfOrder(false)
                    .failOnMissingLocations(false)
                    .cleanDisabled(true)
                    .load();

            flyway.migrate();
            return flyway;
        } catch (FlywayException e) {
            log.error("Flyway migration failed: {}", e.getMessage(), e);
            throw new DatabaseValidationException("Database migration failed: " + e.getMessage(), e);
        }
    }
}
