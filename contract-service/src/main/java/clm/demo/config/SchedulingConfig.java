package clm.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling for background jobs.
 * <p>By annotating with @EnableScheduling, we allow the application to run scheduled tasks defined in
 * other components.</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
