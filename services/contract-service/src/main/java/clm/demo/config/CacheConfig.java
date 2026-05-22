package clm.demo.config;

import clm.demo.cache.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new CaffeineCache(CacheNames.TEMPLATES, Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(Duration.ofHours(1))
                        .recordStats()
                        .build()),
                new CaffeineCache(CacheNames.CONTRACTS, Caffeine.newBuilder()
                        .maximumSize(1_000)
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .recordStats()
                        .build())
        ));
        return manager;
    }
}
