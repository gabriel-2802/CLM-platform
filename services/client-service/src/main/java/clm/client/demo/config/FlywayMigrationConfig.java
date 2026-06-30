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
                    .validateOnMigrate(false)
                    .outOfOrder(false)
                    .failOnMissingLocations(false)
                    .cleanDisabled(true)
                    .ignoreMigrationPatterns("*:pending")
                    .connectRetries(10)
                    .connectRetriesInterval(3)
                    .load();

            // Attempt repair if there's a checksum mismatch
            // This allows migrations to evolve during development
            try {
                flyway.validate();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("checksum mismatch")) {
                    log.warn("Migration checksum mismatch detected, attempting repair");
                    flyway.repair();
                    flyway = Flyway.configure()
                            .dataSource(dataSource)
                            .locations("classpath:db/migration")
                            .baselineOnMigrate(true)
                            .baselineVersion("0")
                            .validateOnMigrate(false)
                            .outOfOrder(false)
                            .failOnMissingLocations(false)
                            .cleanDisabled(true)
                            .ignoreMigrationPatterns("*:pending")
                            .connectRetries(10)
                            .connectRetriesInterval(3)
                            .load();
                } else {
                    throw e;
                }
            }

            flyway.migrate();
            return flyway;
        } catch (FlywayException e) {
            log.error("Flyway migration failed: {}", e.getMessage(), e);
            throw new DatabaseValidationException("Database migration failed: " + e.getMessage(), e);
        }
    }
}
