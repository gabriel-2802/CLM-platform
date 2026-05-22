package clm.demo.cache;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.ContractUpdateRequest;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.RenegotiateContractRequest;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.AppendixStatus;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.ContractService;
import clm.demo.services.TemplateService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration test: Caffeine cache behaviour against a real PostgreSQL instance.
 *
 * Evaluation methodology
 * ─────────────────────
 * Unlike the in-vitro {@link CachePerformanceTest} (which injects artificial latency
 * via Thread.sleep), this class runs against a live PostgreSQL instance with full
 * Flyway schema and real JPA/Hibernate queries.
 *
 * The PostgreSQL container is managed externally by the Makefile target
 * `test-cache-real`, which starts the container before the test and removes it
 * after — regardless of whether the test passes or fails.
 *
 * Connection coordinates are supplied via the `test` Spring profile
 * (application-test.properties: localhost:5434/clm_test).
 *
 * Correctness is proven via Caffeine's internal stats (recordStats() is enabled in
 * CacheConfig):
 *   - missCount() increments every time the cache has no entry and the service
 *     delegates to the database.
 *   - hitCount() increments every time the cache answers without touching the
 *     database.
 * These counters are reset before each test via CacheManager.getCache(name).clear(),
 * which also empties the underlying Caffeine store, guaranteeing a cold start.
 *
 * Latency is measured with System.nanoTime() and asserted directionally
 * (warm < cold) rather than against a hard threshold, because DB round-trip times
 * vary with host hardware.
 *
 * Eviction correctness is verified by checking that the cache entry is absent
 * after a mutating operation, then confirming the next read incurs a fresh miss.
 *
 * Run via:
 *   make test-cache-real
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CacheRealDbTest {

    // ── Guard: skip gracefully when the test DB container is not running ─── //

    @org.junit.jupiter.api.BeforeAll
    static void requireTestDatabase() {
        DriverManager.setLoginTimeout(2);
        try (var ignored = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/clm_test", "clm_user", "clm_pass")) {
            // reachable — proceed
        } catch (SQLException e) {
            assumeTrue(false,
                    "clm-test-pg not reachable on localhost:5434 — run `make test-cache-real` to execute this suite");
        }
    }

    // ── Spring beans ─────────────────────────────────────────────────────── //

    @Autowired TemplateService            templateService;
    @Autowired ContractService            contractService;
    @Autowired DocumentTemplateRepository templateRepository;
    @Autowired ContractRepository         contractRepository;
    @Autowired AppendixRepository         appendixRepository;
    @Autowired CacheManager               cacheManager;

    // ── Per-test fixtures ────────────────────────────────────────────────── //

    DocumentTemplate     seededTemplate;
    Contract             seededContract;
    Appendix             seededAppendix;
    Map<String, long[]>  statsBaseline = new HashMap<>(); // [misses, hits]

    @BeforeEach
    void seedAndClearCaches() {
        clearAllCaches();
        snapshotStats();

        seededTemplate = templateRepository.save(
                DocumentTemplate.builder()
                        .templateName("IT-Template-" + UUID.randomUUID())
                        .documentFormat(DocumentFormat.DOCX)
                        .documentContent(new byte[]{0, 1, 2, 3})
                        .fieldCount(0)
                        .isFullyMapped(true)
                        .build());

        seededContract = contractRepository.save(
                Contract.builder()
                        .clientId(999)
                        .contractStatus(ContractStatus.ACTIVE)
                        .build());

        seededAppendix = appendixRepository.save(
                Appendix.builder()
                        .contract(seededContract)
                        .title("Test Appendix")
                        .appendixStatus(AppendixStatus.SIGNED)
                        .build());
    }

    @AfterEach
    void cleanup() {
        clearAllCaches();
        // Delete contract first: cascades to contract_details and appendix.
        // Explicit appendix deletion would violate the RESTRICT FK from contract_details.
        if (seededContract != null) {
            contractRepository.deleteById(seededContract.getId());
            seededContract  = null;
            seededAppendix  = null;
        }
        if (seededTemplate != null) {
            templateRepository.deleteById(seededTemplate.getId());
            seededTemplate = null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────── //

    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var c = cacheManager.getCache(name);
            if (c != null) c.clear();
        });
    }

    /**
     * Snapshot current cumulative Caffeine stats. Caffeine stats never reset,
     * so each test records a baseline and computes deltas via missCount/hitCount.
     */
    private void snapshotStats() {
        cacheManager.getCacheNames().forEach(name -> {
            CacheStats s = rawStats(name);
            statsBaseline.put(name, new long[]{s.missCount(), s.hitCount()});
        });
    }

    private CacheStats rawStats(String cacheName) {
        var cache = (CaffeineCache) cacheManager.getCache(cacheName);
        return cache.getNativeCache().stats();
    }

    /** Misses recorded since the last @BeforeEach snapshot. */
    private long missCount(String cacheName) {
        return rawStats(cacheName).missCount() - statsBaseline.getOrDefault(cacheName, new long[2])[0];
    }

    /** Hits recorded since the last @BeforeEach snapshot. */
    private long hitCount(String cacheName) {
        return rawStats(cacheName).hitCount() - statsBaseline.getOrDefault(cacheName, new long[2])[1];
    }

    /**
     * Returns true when the named cache has no entry for the given key.
     * Uses asMap().containsKey() instead of cache.get() so Caffeine does not
     * record the probe as a hit or miss — keeping stats clean for assertions.
     */
    private boolean isAbsent(String cacheName, Object key) {
        var springCache = cacheManager.getCache(cacheName);
        if (springCache == null) return true;
        return !((CaffeineCache) springCache).getNativeCache().asMap().containsKey(key);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  templates cache — against a real PostgreSQL instance
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("templates cache — real PostgreSQL")
    class TemplateCacheTests {

        @Test
        @DisplayName("second read is a cache hit — Caffeine records 1 miss then 1 hit")
        void second_read_is_cache_hit() {
            Long id = seededTemplate.getId();

            templateService.getTemplate(id); // cold — DB fetch, populates cache
            templateService.getTemplate(id); // warm — served from cache

            assertThat(missCount(CacheNames.TEMPLATES)).isEqualTo(1);
            assertThat(hitCount(CacheNames.TEMPLATES)).isEqualTo(1);
        }

        @Test
        @DisplayName("warm read is faster than a cold DB fetch")
        void warm_read_is_faster_than_cold() {
            Long id = seededTemplate.getId();

            long t0    = System.nanoTime();
            templateService.getTemplate(id);
            long coldNs = System.nanoTime() - t0;

            long t1    = System.nanoTime();
            templateService.getTemplate(id);
            long warmNs = System.nanoTime() - t1;

            System.out.printf("[TEMPLATE real-db] cold=%,d µs  warm=%,d µs  speedup=%.1f×%n",
                    coldNs / 1_000, warmNs / 1_000,
                    (double) coldNs / Math.max(warmNs, 1));

            assertThat(warmNs)
                    .as("cache-hit latency must be less than cold DB fetch latency")
                    .isLessThan(coldNs);
        }

        @Test
        @DisplayName("N repeated reads produce exactly 1 miss and N-1 hits")
        void repeated_reads_produce_one_miss_and_n_minus_one_hits() {
            int repeats = 10;
            Long id = seededTemplate.getId();

            for (int i = 0; i < repeats; i++) {
                templateService.getTemplate(id);
            }

            assertThat(missCount(CacheNames.TEMPLATES)).isEqualTo(1);
            assertThat(hitCount(CacheNames.TEMPLATES)).isEqualTo(repeats - 1);
        }

        @Test
        @DisplayName("deleteTemplate evicts the entry — cache is empty, next read is a miss")
        void delete_evicts_cache_entry() {
            Long id = seededTemplate.getId();

            templateService.getTemplate(id); // miss #1 — populate
            templateService.getTemplate(id); // hit  #1
            assertThat(isAbsent(CacheNames.TEMPLATES, id)).isFalse(); // entry present

            templateService.deleteTemplate(id);
            seededTemplate = null;  // already deleted; skip @AfterEach cleanup

            assertThat(isAbsent(CacheNames.TEMPLATES, id))
                    .as("cache must not hold a stale entry after deleteTemplate")
                    .isTrue();
        }

        @Test
        @DisplayName("updateFieldLabels evicts the entry — next read incurs a second miss")
        void update_field_labels_evicts_cache_entry() {
            Long id = seededTemplate.getId();

            templateService.getTemplate(id); // miss #1 — populate

            var request = new FieldMappingRequest();
            request.setTemplateId(id);
            request.setMappings(Collections.emptyList());
            templateService.updateFieldLabels(request); // evict

            assertThat(isAbsent(CacheNames.TEMPLATES, id))
                    .as("cache must not hold a stale entry after updateFieldLabels")
                    .isTrue();

            templateService.getTemplate(id); // miss #2 — re-fetches from DB

            assertThat(missCount(CacheNames.TEMPLATES)).isEqualTo(2);
            assertThat(hitCount(CacheNames.TEMPLATES)).isEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  contracts cache — against a real PostgreSQL instance
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("contracts cache — real PostgreSQL")
    class ContractCacheTests {

        @Test
        @DisplayName("second read is a cache hit — Caffeine records 1 miss then 1 hit")
        void second_read_is_cache_hit() {
            Long id = seededContract.getId();

            contractService.getById(id); // cold
            contractService.getById(id); // warm

            assertThat(missCount(CacheNames.CONTRACTS)).isEqualTo(1);
            assertThat(hitCount(CacheNames.CONTRACTS)).isEqualTo(1);
        }

        @Test
        @DisplayName("warm read is faster than a cold DB fetch")
        void warm_read_is_faster_than_cold() {
            Long id = seededContract.getId();

            long t0    = System.nanoTime();
            contractService.getById(id);
            long coldNs = System.nanoTime() - t0;

            long t1    = System.nanoTime();
            contractService.getById(id);
            long warmNs = System.nanoTime() - t1;

            System.out.printf("[CONTRACT real-db] cold=%,d µs  warm=%,d µs  speedup=%.1f×%n",
                    coldNs / 1_000, warmNs / 1_000,
                    (double) coldNs / Math.max(warmNs, 1));

            assertThat(warmNs)
                    .as("cache-hit latency must be less than cold DB fetch latency")
                    .isLessThan(coldNs);
        }

        @Test
        @DisplayName("N repeated reads produce exactly 1 miss and N-1 hits")
        void repeated_reads_produce_one_miss_and_n_minus_one_hits() {
            int repeats = 10;
            Long id = seededContract.getId();

            for (int i = 0; i < repeats; i++) {
                contractService.getById(id);
            }

            assertThat(missCount(CacheNames.CONTRACTS)).isEqualTo(1);
            assertThat(hitCount(CacheNames.CONTRACTS)).isEqualTo(repeats - 1);
        }

        @Test
        @DisplayName("terminateContract evicts entry — cache is empty, next read is a miss")
        void terminate_evicts_cache_entry() {
            Long id = seededContract.getId();

            assertThat(isAbsent(CacheNames.CONTRACTS, id)).isFalse();

            contractService.terminateContract(id,
                    new ContractTerminationRequest(LocalDate.now(), 1, "integration test"));

            assertThat(isAbsent(CacheNames.CONTRACTS, id))
                    .as("cache must not hold a stale entry after terminateContract")
                    .isTrue();

            contractService.getById(id); // miss #2

            assertThat(missCount(CacheNames.CONTRACTS)).isEqualTo(2);
            assertThat(hitCount(CacheNames.CONTRACTS)).isEqualTo(0);
        }

        @Test
        @DisplayName("updateContractTerms evicts entry — cache is empty, next read is a miss")
        void update_terms_evicts_cache_entry() {
            Long id = seededContract.getId();

            contractService.getById(id); // miss #1 — populate
            assertThat(isAbsent(CacheNames.CONTRACTS, id)).isFalse();

            contractService.updateContractTerms(id,
                    new ContractUpdateRequest(1, seededAppendix.getId().intValue(), LocalDate.of(2028, 6, 1), null, null, null));

            assertThat(isAbsent(CacheNames.CONTRACTS, id))
                    .as("cache must not hold a stale entry after updateContractTerms")
                    .isTrue();

            contractService.getById(id); // miss #2

            assertThat(missCount(CacheNames.CONTRACTS)).isEqualTo(2);
            assertThat(hitCount(CacheNames.CONTRACTS)).isEqualTo(0);
        }
    }
}
