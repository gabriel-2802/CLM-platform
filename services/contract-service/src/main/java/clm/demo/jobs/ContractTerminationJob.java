package clm.demo.jobs;

import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduled job that processes contracts due for termination.
 *
 * <p>Runs daily at midnight (configurable via {@code job.process-termination.cron}).
 * Transitions every {@link ContractStatus#TERMINATION_DUE} contract whose
 * {@code terminationDate == today} to {@link ContractStatus#TERMINATED}.</p>
 *
 * <p>Uses a direct database update query for efficiency — no contracts are loaded into memory.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractTerminationJob {

    private final ContractRepository contractRepository;

    @Scheduled(cron = "${job.process-termination.cron:0 0 0 * * *}")
    public void processTerminationDueContracts() {
        int count = contractRepository.processTerminationDueContracts(
                ContractStatus.TERMINATED,
                ContractStatus.TERMINATION_DUE,
                LocalDate.now()
        );
        log.info("Processed {} termination-due contract(s) to TERMINATED", count);
    }
}

