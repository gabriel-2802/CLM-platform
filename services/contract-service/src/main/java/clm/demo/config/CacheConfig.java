package clm.demo.config;

import clm.demo.cache.CacheNames;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    /**
     * templates  — read-mostly; template binary content can be tens of MB.
     *              Max 200 entries, evict 1 h after write.
     *
     * contracts  — higher churn (status changes, renegotiations).
     *              Max 1 000 entries, evict 10 min after write.
     *
     * Each native cache is registered with Micrometer via CaffeineCacheMetrics,
     * which publishes the following meters to /actuator/metrics and /actuator/prometheus:
     *   cache.gets          {name, result=hit|miss}
     *   cache.evictions     {name}
     *   cache.size          {name}
     *   cache.puts          {name}
     */
    @Bean
    public CacheManager cacheManager(MeterRegistry meterRegistry) {
        Cache<Object, Object> templatesNative = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofHours(1))
                .recordStats()
                .build();

        Cache<Object, Object> contractsNative = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();

        CaffeineCacheMetrics.monitor(meterRegistry, templatesNative, CacheNames.TEMPLATES);
        CaffeineCacheMetrics.monitor(meterRegistry, contractsNative, CacheNames.CONTRACTS);

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new CaffeineCache(CacheNames.TEMPLATES, templatesNative),
                new CaffeineCache(CacheNames.CONTRACTS, contractsNative)
        ));
        return manager;
    }
}
