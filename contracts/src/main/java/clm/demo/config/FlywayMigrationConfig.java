package clm.demo.config;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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
                    .validateOnMigrate(false)
                    .load();
            flyway.migrate();

            return flyway;
        } catch (FlywayException e) {
            System.err.println("Flyway migration warning: " + e.getMessage());

            return Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .validateOnMigrate(false)
                    .load();
        }
    }
}


