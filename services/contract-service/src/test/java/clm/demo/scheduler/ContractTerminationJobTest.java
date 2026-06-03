package clm.demo.scheduler;

import clm.demo.events.ContractDeactivatedEvent;
import clm.demo.jobs.ContractTerminationJob;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractTerminationJobTest {

    @Mock ContractRepository contractRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ContractTerminationJob job;

    @Test
    void should_terminate_contracts_and_publish_deactivation_event() {
        List<Long> contractIds = List.of(1L, 2L);
        when(contractRepository.findTerminationDueContractIds(any(), any(), any()))
                .thenReturn(contractIds)
                .thenReturn(new ArrayList<>()); // Empty on second batch

        job.processTerminationDueContracts();

        // Verify batch was terminated
        verify(contractRepository).terminateContractsByIds(contractIds, ContractStatus.TERMINATED);

        // Verify deactivation event was published
        ArgumentCaptor<ContractDeactivatedEvent> eventCaptor =
                ArgumentCaptor.forClass(ContractDeactivatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getValue().contractIds()).isEqualTo(contractIds);
    }

    @Test
    void should_not_throw_when_no_contracts_due() {
        when(contractRepository.findTerminationDueContractIds(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        job.processTerminationDueContracts(); // must complete without throwing

        verify(contractRepository, times(1))
                .findTerminationDueContractIds(any(), any(), any());
        verify(eventPublisher, times(0)).publishEvent(any());
    }

    @Test
    void should_process_multiple_batches() {
        // Create batch1 with exactly BATCH_SIZE items to trigger another iteration
        List<Long> batch1 = new ArrayList<>();
        for (long i = 1; i <= 50; i++) {
            batch1.add(i);
        }
        // Create batch2 with fewer items to end the loop
        List<Long> batch2 = List.of(51L, 52L);

        when(contractRepository.findTerminationDueContractIds(any(), any(), any()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(new ArrayList<>());

        job.processTerminationDueContracts();

        // Verify both batches were terminated
        verify(contractRepository).terminateContractsByIds(batch1, ContractStatus.TERMINATED);
        verify(contractRepository).terminateContractsByIds(batch2, ContractStatus.TERMINATED);

        // Verify events were published for both batches
        verify(eventPublisher, times(2)).publishEvent(any(ContractDeactivatedEvent.class));
    }

    @Test
    void should_complete_asynchronously_within_time_budget() {
        AtomicBoolean called = new AtomicBoolean(false);
        doAnswer(inv -> {
            called.set(true);
            return new ArrayList<>();
        }).when(contractRepository).findTerminationDueContractIds(any(), any(), any());

        Thread t = new Thread(job::processTerminationDueContracts);
        t.start();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilTrue(called);
    }
}
