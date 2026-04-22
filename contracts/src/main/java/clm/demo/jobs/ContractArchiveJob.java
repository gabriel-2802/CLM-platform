package clm.demo.jobs;

import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduled job that archives contracts whose end date has passed.
 *
 * <p>Runs daily at midnight (configurable via {@code job.archive-contracts.cron}).
 * Transitions every {@link ContractStatus#ACTIVE} contract whose
 * {@code contractEndDate < today} to {@link ContractStatus#ARCHIVED}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractArchiveJob {

    private final ContractRepository contractRepository;

    @Scheduled(cron = "${job.archive-contracts.cron:0 0 0 * * *}")
    public void archiveExpiredContracts() {
        int count = contractRepository.archiveExpiredContracts(
                ContractStatus.ARCHIVED,
                ContractStatus.ACTIVE,
                LocalDate.now()
        );
        log.info("Archived {} expired contract(s)", count);
    }
}
