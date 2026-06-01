package clm.demo.scheduler;

import clm.demo.jobs.ContractArchiveJob;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractArchiveJobTest {

    @Mock ContractRepository contractRepository;

    @InjectMocks ContractArchiveJob job;

    // ── Direct invocation unit tests ─────────────────────────────────────────

    @Test
    void should_call_archive_with_today_active_and_archived_statuses() {
        when(contractRepository.archiveExpiredContracts(any(), any(), any())).thenReturn(3);

        job.archiveExpiredContracts();

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<ContractStatus> activeCaptor = ArgumentCaptor.forClass(ContractStatus.class);
        ArgumentCaptor<ContractStatus> archivedCaptor = ArgumentCaptor.forClass(ContractStatus.class);

        verify(contractRepository).archiveExpiredContracts(
                dateCaptor.capture(), activeCaptor.capture(), archivedCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
        assertThat(activeCaptor.getValue()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(archivedCaptor.getValue()).isEqualTo(ContractStatus.ARCHIVED);
    }

    @Test
    void should_process_zero_contracts_when_none_expired() {
        when(contractRepository.archiveExpiredContracts(any(), any(), any())).thenReturn(0);

        job.archiveExpiredContracts(); // must complete without throwing

        verify(contractRepository).archiveExpiredContracts(any(), any(), any());
    }

    @Test
    void should_process_multiple_expired_contracts() {
        when(contractRepository.archiveExpiredContracts(any(), any(), any())).thenReturn(42);

        job.archiveExpiredContracts();

        verify(contractRepository).archiveExpiredContracts(
                eq(LocalDate.now()),
                eq(ContractStatus.ACTIVE),
                eq(ContractStatus.ARCHIVED));
    }

    // ── Awaitility-based async invocation test ────────────────────────────────

    @Nested
    class AwaitilityPattern {

        /**
         * Demonstrates the Awaitility pattern for asserting side effects
         * when a scheduled job completes asynchronously.
         *
         * Here we call the method directly and use Awaitility to verify
         * the side-effect (repository call) occurs within the time budget.
         * In a real Spring context test, the scheduler would trigger the call.
         */
        @Test
        void should_invoke_archive_query_within_expected_time() {
            AtomicInteger callCount = new AtomicInteger(0);
            doAnswer(inv -> { callCount.incrementAndGet(); return 0; })
                    .when(contractRepository).archiveExpiredContracts(any(), any(), any());

            Thread schedulerThread = new Thread(job::archiveExpiredContracts);
            schedulerThread.start();

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(callCount.get()).isEqualTo(1));
        }
    }
}
