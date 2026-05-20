package clm.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class to enable scheduling for background jobs.
 * <p>By annotating with @EnableScheduling, we allow the application to run scheduled tasks defined in
 * other components.</p>
 * <p>Configured with a ThreadPoolTaskScheduler to allow multiple scheduled tasks to run in parallel.</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Configures a ThreadPoolTaskScheduler to run multiple scheduled tasks in parallel.
     * <p>With a pool size of 5, up to 5 scheduled tasks can execute simultaneously.</p>
     *
     * @return configured ThreadPoolTaskScheduler
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.initialize();
        return scheduler;
    }
}

