package clm.demo.config;

import clm.demo.exceptions.exceptions.DatabaseValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Configuration for Flyway database migration
 *
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlywayMigrationConfig {

    private final Environment environment;

    /**
     * Create and run Flyway migrations programmatically.
     * This bean ensures migrations run BEFORE JPA initialization.
     * 
     * @param dataSource the DataSource configured in properties
     * @return Flyway instance that has already executed migrations
     */
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
                    .connectRetries(10)
                    .connectRetriesInterval(3)
                    .load();

            // repair flyway metadata if needed
            repair(flyway);
            flyway.migrate();

            return flyway;
        } catch (FlywayException e) {
            log.error("Flyway migration failed: {}", e.getMessage(), e);
            throw new DatabaseValidationException("Database migration failed: " + e.getMessage(), e);
        }
    }

    private void repair(Flyway flyway) {
        try {
            flyway.repair();
        } catch (Exception e) {
            log.info("Flyway repair failed (might be expected if no issues): {}", e.getMessage());
        }
    }
}


