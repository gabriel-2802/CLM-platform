package clm.demo.scheduler;

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

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractTerminationJobTest {

    @Mock ContractRepository contractRepository;

    @InjectMocks ContractTerminationJob job;

    @Test
    void should_call_process_with_terminated_termination_due_and_today() {
        when(contractRepository.processTerminationDueContracts(any(), any(), any())).thenReturn(2);

        job.processTerminationDueContracts();

        ArgumentCaptor<ContractStatus> terminatedCaptor = ArgumentCaptor.forClass(ContractStatus.class);
        ArgumentCaptor<ContractStatus> dueCaptor        = ArgumentCaptor.forClass(ContractStatus.class);
        ArgumentCaptor<LocalDate>      dateCaptor        = ArgumentCaptor.forClass(LocalDate.class);

        verify(contractRepository).processTerminationDueContracts(
                terminatedCaptor.capture(), dueCaptor.capture(), dateCaptor.capture());

        assertThat(terminatedCaptor.getValue()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(dueCaptor.getValue()).isEqualTo(ContractStatus.TERMINATION_DUE);
        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void should_not_throw_when_no_contracts_due() {
        when(contractRepository.processTerminationDueContracts(any(), any(), any())).thenReturn(0);

        job.processTerminationDueContracts(); // must complete without throwing
    }

    @Test
    void should_not_throw_when_many_contracts_terminated() {
        when(contractRepository.processTerminationDueContracts(any(), any(), any())).thenReturn(100);

        job.processTerminationDueContracts();
    }

    @Test
    void should_complete_asynchronously_within_time_budget() {
        AtomicBoolean called = new AtomicBoolean(false);
        doAnswer(inv -> { called.set(true); return 0; })
                .when(contractRepository).processTerminationDueContracts(any(), any(), any());

        Thread t = new Thread(job::processTerminationDueContracts);
        t.start();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilTrue(called);
    }
}
