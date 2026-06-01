package clm.demo.scheduler;

import clm.demo.jobs.ContractArchiveJob;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractArchiveJobTest {

    @Mock ContractRepository contractRepository;

    @InjectMocks ContractArchiveJob job;

    @Test
    void should_archive_single_batch_and_log_ids() {
        List<Long> ids = List.of(1L, 2L, 3L);
        when(contractRepository.findExpiredContractIds(any(), any(), any()))
                .thenReturn(ids)
                .thenReturn(List.of());

        job.archiveExpiredContracts();

        verify(contractRepository).archiveContractsByIds(eq(ids), eq(ContractStatus.ARCHIVED));
    }

    @Test
    void should_do_nothing_when_no_expired_contracts() {
        when(contractRepository.findExpiredContractIds(any(), any(), any())).thenReturn(List.of());

        job.archiveExpiredContracts();

        verify(contractRepository, never()).archiveContractsByIds(any(), any());
    }

    @Test
    void should_process_multiple_batches_until_partial() {
        List<Long> fullBatch = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L,
                31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L,
                41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L);
        List<Long> lastBatch = List.of(51L, 52L);

        when(contractRepository.findExpiredContractIds(any(), any(), any()))
                .thenReturn(fullBatch)
                .thenReturn(lastBatch);

        job.archiveExpiredContracts();

        verify(contractRepository, times(2)).findExpiredContractIds(
                eq(LocalDate.now()), eq(ContractStatus.ACTIVE), eq(PageRequest.of(0, 50)));
        verify(contractRepository).archiveContractsByIds(eq(fullBatch), eq(ContractStatus.ARCHIVED));
        verify(contractRepository).archiveContractsByIds(eq(lastBatch), eq(ContractStatus.ARCHIVED));
    }

    @Test
    void should_use_today_and_active_status_when_querying() {
        when(contractRepository.findExpiredContractIds(any(), any(), any())).thenReturn(List.of());

        job.archiveExpiredContracts();

        verify(contractRepository).findExpiredContractIds(
                eq(LocalDate.now()), eq(ContractStatus.ACTIVE), eq(PageRequest.of(0, 50)));
    }

    @Nested
    class AwaitilityPattern {

        @Test
        void should_invoke_archive_query_within_expected_time() {
            AtomicInteger callCount = new AtomicInteger(0);
            doAnswer(inv -> { callCount.incrementAndGet(); return List.of(); })
                    .when(contractRepository).findExpiredContractIds(any(), any(), any());

            Thread schedulerThread = new Thread(job::archiveExpiredContracts);
            schedulerThread.start();

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(callCount.get()).isEqualTo(1));
        }
    }
}
