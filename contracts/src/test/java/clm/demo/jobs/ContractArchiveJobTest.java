package clm.demo.jobs;

import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * verifies that ContractArchiveJob correctly invokes the bulk-archive query with the
 * right statuses and today's date as the cutoff.
 */
@ExtendWith(MockitoExtension.class)
class ContractArchiveJobTest {

    @Mock ContractRepository contractRepository;

    @InjectMocks ContractArchiveJob job;

    @Test
    void archives_active_contracts_with_today_as_cutoff() {
        when(contractRepository.archiveExpiredContracts(any(), any(), any())).thenReturn(3);

        job.archiveExpiredContracts();

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(contractRepository).archiveExpiredContracts(
                eq(ContractStatus.ARCHIVED),
                eq(ContractStatus.ACTIVE),
                dateCaptor.capture()
        );
        // cutoff must be today — job runs at midnight
        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void zero_archived_does_not_throw() {
        when(contractRepository.archiveExpiredContracts(any(), any(), any())).thenReturn(0);

        // must complete without exception even when nothing is archived
        job.archiveExpiredContracts();

        verify(contractRepository).archiveExpiredContracts(any(), any(), any());
    }

    @Test
    void passes_archived_as_target_status_and_active_as_source_status() {
        when(contractRepository.archiveExpiredContracts(any(), any(), any())).thenReturn(1);

        job.archiveExpiredContracts();

        verify(contractRepository).archiveExpiredContracts(
                eq(ContractStatus.ARCHIVED),
                eq(ContractStatus.ACTIVE),
                any()
        );
    }
}
