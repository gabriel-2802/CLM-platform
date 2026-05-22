package clm.demo.cache;

import clm.demo.config.CacheConfig;
import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.ContractUpdateRequest;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.RenegotiateContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.mappers.ContractDetailsMapper;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.DocumentTemplateMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractDetailsRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import clm.demo.services.ContractService;
import clm.demo.services.TemplateService;
import clm.demo.services.utility.DocumentGenerationUtil;
import clm.demo.specifications.ContractSpecification;
import clm.demo.support.TestDataFactory;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * In-vitro Caffeine cache performance tests,  no database, no Flyway.
 *
 * Design
 * ------
 * A minimal Spring context loads only TemplateService + ContractService + CacheConfig.
 * All repositories are Mockito mocks; a mock PlatformTransactionManager satisfies
 * the @Transactional AOP interceptor without a real datasource.
 *
 * Each scenario proves two independent properties:
 *   1. Correctness -> the repository is called exactly once per unique cache key,
 *      regardless of how many times the service method is invoked.
 *   2. Latency   -> a cache hit completes within CACHE_HIT_THRESHOLD_MS, while the
 *      initial cold call takes at least SIMULATED_DB_LATENCY_MS.
 *
 * Eviction tests verify that mutating operations (delete, update, terminate, …)
 * remove the stale entry so the very next read re-fetches from the "database".
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CachePerformanceTest.TestConfig.class)
class CachePerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(CachePerformanceTest.class);

    @Configuration
    @EnableCaching
    @Import({CacheConfig.class, TemplateService.class, ContractService.class})
    static class TestConfig {
        /** Satisfies the @Transactional AOP interceptor without a real datasource. */
        @Bean
        PlatformTransactionManager transactionManager() {
            return Mockito.mock(PlatformTransactionManager.class);
        }
        /** Satisfies CacheConfig's MeterRegistry dependency without Actuator on the classpath. */
        @Bean
        io.micrometer.core.instrument.MeterRegistry meterRegistry() {
            return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        }
    }

    /** mocks */

    @MockitoBean DocumentTemplateRepository   templateRepository;
    @MockitoBean TemplateFieldRepository      templateFieldRepository;
    @MockitoBean ContractRepository           contractRepository;
    @MockitoBean ContractDetailsRepository    contractDetailsRepository;
    @MockitoBean DocumentFieldValueRepository fieldValueRepository;
    @MockitoBean AppendixRepository           appendixRepository;
    @MockitoBean ContractGenerationMapper     contractGenerationMapper;
    @MockitoBean GeneratedContractMapper      generatedContractMapper;
    @MockitoBean ContractDetailsMapper        contractDetailsMapper;
    @MockitoBean ContractSpecification        contractSpecification;
    @MockitoBean DocumentTemplateMapper       documentTemplateMapper;
    @MockitoBean FileUtils                    fileUtils;
    @MockitoBean DocumentGenerationUtil       documentGenerationUtil;

    @Autowired TemplateService templateService;
    @Autowired ContractService contractService;
    @Autowired CacheManager    cacheManager;

    /** Simulated DB round-trip latency injected via mock answer (ms). */
    private static final long DB_LATENCY_MS        = 50;
    /** Maximum acceptable wall-clock time for a cache-hit call (ms). */
    private static final long CACHE_HIT_MAX_MS     = 10;

    @BeforeEach
    void clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var c = cacheManager.getCache(name);
            if (c != null) c.clear();
        });
    }

    //  templates cache

    @Nested
    @DisplayName("templates cache")
    class TemplateCacheTests {

        private static final Long TEMPLATE_ID = 1L;

        private DocumentTemplate stubTemplate;

        @BeforeEach
        void stub() {
            stubTemplate = TestDataFactory.template(TEMPLATE_ID);
            var stubResponseDTO = TemplateResponseDTO.builder()
                    .templateId(TEMPLATE_ID)
                    .templateName("Service Agreement")
                    .fieldCount(5)
                    .fullyMapped(true)
                    .build();

            when(templateRepository.findById(TEMPLATE_ID)).thenAnswer(inv -> {
                Thread.sleep(DB_LATENCY_MS);
                return Optional.of(stubTemplate);
            });
            when(documentTemplateMapper.toResponseDTO(any())).thenReturn(stubResponseDTO);
        }

        @Test
        @DisplayName("second getTemplate call is a cache hit -> repository called exactly once")
        void secondCallIsACacheHit() {
            templateService.getTemplate(TEMPLATE_ID);
            templateService.getTemplate(TEMPLATE_ID);

            verify(templateRepository, times(1)).findById(TEMPLATE_ID);
        }

        @Test
        @DisplayName("cache hit completes under threshold; cold call exceeds simulated DB latency")
        void cacheHitIsSignificantlyFaster() {
            long t0 = System.currentTimeMillis();
            templateService.getTemplate(TEMPLATE_ID);
            long coldMs = System.currentTimeMillis() - t0;

            long t1 = System.currentTimeMillis();
            templateService.getTemplate(TEMPLATE_ID);
            long warmMs = System.currentTimeMillis() - t1;

            log.info("[TEMPLATE] cold={} ms  warm={} ms  speedup={}",
                    coldMs, warmMs, (double) coldMs / Math.max(warmMs, 1));

            assertThat(coldMs).as("cold call must suffer simulated DB latency")
                    .isGreaterThanOrEqualTo(DB_LATENCY_MS);
            assertThat(warmMs).as("warm (cache-hit) call must be near zero")
                    .isLessThan(CACHE_HIT_MAX_MS);
        }

        @Test
        @DisplayName("deleteTemplate evicts the cache entry -> next read re-fetches from DB")
        void deleteEvictsCache() {
            when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);

            templateService.getTemplate(TEMPLATE_ID);            // DB call #1 -> populates cache
            templateService.getTemplate(TEMPLATE_ID);            // cache hit, no DB call
            templateService.deleteTemplate(TEMPLATE_ID);         // evict

            templateService.getTemplate(TEMPLATE_ID);            // DB call #2 -> re-fetch
            verify(templateRepository, times(2)).findById(TEMPLATE_ID);
        }

        @Test
        @DisplayName("updateFieldLabels evicts the cache entry -> next read re-fetches from DB")
        void updateFieldLabelsEvictsCache() {
            when(templateRepository.existsById(TEMPLATE_ID)).thenReturn(true);
            when(templateRepository.findById(TEMPLATE_ID)).thenAnswer(inv -> {
                Thread.sleep(DB_LATENCY_MS);
                return Optional.of(stubTemplate);
            });
            when(templateFieldRepository.findAllById(anyCollection())).thenReturn(List.of());
            when(templateFieldRepository.saveAll(any())).thenReturn(List.of());
            when(templateRepository.save(any())).thenReturn(stubTemplate);

            templateService.getTemplate(TEMPLATE_ID);            // DB call #1 -> populates cache

            // updateFieldLabels: passes empty mappings (no @Valid on the parameter),
            // calls findById internally for updateMappingStatus -> this is DB call #2.
            var emptyRequest = new FieldMappingRequest();
            emptyRequest.setTemplateId(TEMPLATE_ID);
            emptyRequest.setMappings(Collections.emptyList());
            templateService.updateFieldLabels(emptyRequest);     // evicts; internal DB call #2

            templateService.getTemplate(TEMPLATE_ID);            // DB call #3 -> re-fetch after eviction
            verify(templateRepository, times(3)).findById(TEMPLATE_ID);
        }

        @Test
        @DisplayName("N distinct template IDs each cache independently -> only N DB calls total for 2N reads")
        void distinctIdsCachedIndependently()  {
            long id2 = 2L;
            var tpl2 = TestDataFactory.template(id2);
            var dto2 = TemplateResponseDTO.builder().templateId(id2).templateName("NDA").build();

            when(templateRepository.findById(id2)).thenAnswer(inv -> {
                Thread.sleep(DB_LATENCY_MS);
                return Optional.of(tpl2);
            });
            when(documentTemplateMapper.toResponseDTO(tpl2)).thenReturn(dto2);

            // First round -> both miss
            templateService.getTemplate(TEMPLATE_ID);
            templateService.getTemplate(id2);
            // Second round -> both hit
            templateService.getTemplate(TEMPLATE_ID);
            templateService.getTemplate(id2);

            verify(templateRepository, times(1)).findById(TEMPLATE_ID);
            verify(templateRepository, times(1)).findById(id2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  contracts cache
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("contracts cache")
    class ContractCacheTests {

        private static final Long CONTRACT_ID = 10L;

        @BeforeEach
        void stub() {
            var contract = TestDataFactory.contract(CONTRACT_ID, ContractStatus.ACTIVE);
            var stubResponseDTO = ContractResponseDTO.builder()
                    .id(CONTRACT_ID)
                    .clientId(42)
                    .contractStatus("ACTIVE")
                    .contractValue(BigDecimal.valueOf(10_000))
                    .contractStartDate(LocalDate.of(2026, 1, 1))
                    .contractEndDate(LocalDate.of(2027, 1, 1))
                    .build();

            when(contractRepository.findById(CONTRACT_ID)).thenAnswer(inv -> {
                Thread.sleep(DB_LATENCY_MS);
                return Optional.of(contract);
            });
            when(generatedContractMapper.toResponseDTO(any(Contract.class), any(ContractDetails.class))).thenReturn(stubResponseDTO);
        }

        @Test
        @DisplayName("second getById call is a cache hit -> repository called exactly once")
        void secondCallIsACacheHit() {
            contractService.getById(CONTRACT_ID);
            contractService.getById(CONTRACT_ID);

            verify(contractRepository, times(1)).findById(CONTRACT_ID);
        }

        @Test
        @DisplayName("cache hit completes under threshold; cold call exceeds simulated DB latency")
        void cacheHitIsSignificantlyFaster() {
            long t0 = System.currentTimeMillis();
            contractService.getById(CONTRACT_ID);
            long coldMs = System.currentTimeMillis() - t0;

            long t1 = System.currentTimeMillis();
            contractService.getById(CONTRACT_ID);
            long warmMs = System.currentTimeMillis() - t1;

            log.info("[CONTRACT] cold={} ms  warm={} ms  speedup={}",
                    coldMs, warmMs, (double) coldMs / Math.max(warmMs, 1));

            assertThat(coldMs).as("cold call must suffer simulated DB latency")
                    .isGreaterThanOrEqualTo(DB_LATENCY_MS);
            assertThat(warmMs).as("warm (cache-hit) call must be near zero")
                    .isLessThan(CACHE_HIT_MAX_MS);
        }

        @Test
        @DisplayName("updateContractTerms evicts the cache entry -> next read re-fetches from DB")
        void updateTermsEvictsCache() {
            var contract = TestDataFactory.contract(CONTRACT_ID, ContractStatus.ACTIVE);
            when(contractRepository.findById(CONTRACT_ID)).thenAnswer(inv -> {
                Thread.sleep(DB_LATENCY_MS);
                return Optional.of(contract);
            });
            when(contractRepository.save(any())).thenReturn(contract);

            when(appendixRepository.findById(any())).thenReturn(Optional.of(mock(Appendix.class)));
            contractService.getById(CONTRACT_ID);           // DB call #1 (cache miss)
            contractService.updateContractTerms(CONTRACT_ID,
                    new ContractUpdateRequest(1, 1, LocalDate.of(2026, 1, 1), LocalDate.of(2028, 6, 1), null, null));
            // ↑ updateContractTerms loads the contract internally via findById (#2), then evicts the cache.

            contractService.getById(CONTRACT_ID);           // DB call #3 (cache was evicted)
            verify(contractRepository, times(3)).findById(CONTRACT_ID);
        }

        @Test
        @DisplayName("terminateContract evicts the cache entry -> next read re-fetches from DB")
        void terminateEvictsCache() {
            var contract = TestDataFactory.contract(CONTRACT_ID, ContractStatus.ACTIVE);
            when(contractRepository.findById(CONTRACT_ID)).thenAnswer(inv -> {
                Thread.sleep(DB_LATENCY_MS);
                return Optional.of(contract);
            });
            when(contractRepository.save(any())).thenReturn(contract);

            contractService.getById(CONTRACT_ID);           // DB call #1 (cache miss)
            contractService.terminateContract(CONTRACT_ID,
                    new ContractTerminationRequest(LocalDate.now(), 1, "end of service"));
            // ↑ terminateContract loads the contract internally via findById (#2), then evicts the cache.

            contractService.getById(CONTRACT_ID);           // DB call #3 (cache was evicted)
            verify(contractRepository, times(3)).findById(CONTRACT_ID);
        }

        @Test
        @DisplayName("N contracts each cached independently -> only N DB calls for N×repeat reads")
        void nContractsCachedIndependently() {
            int count   = 5;
            int repeats = 3;

            for (long id = 1; id <= count; id++) {
                final long cid = id;
                var c = TestDataFactory.contract(cid, ContractStatus.ACTIVE);
                when(contractRepository.findById(cid)).thenAnswer(inv -> {
                    Thread.sleep(DB_LATENCY_MS);
                    return Optional.of(c);
                });
            }

            for (int r = 0; r < repeats; r++) {
                for (long id = 1; id <= count; id++) {
                    contractService.getById(id);
                }
            }

            for (long id = 1; id <= count; id++) {
                verify(contractRepository, times(1)).findById(id);
            }
        }
    }
}
