package clm.demo.jobs;

import clm.demo.events.ContractDeactivatedEvent;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled job that processes contracts due for termination.
 *
 * <p>Runs daily at midnight (configurable via {@code job.process-termination.cron}).
 * Transitions every {@link ContractStatus#TERMINATION_DUE} contract whose
 * {@code terminationDate == today} to {@link ContractStatus#TERMINATED}, then publishes
 * a {@link ContractDeactivatedEvent} for downstream services.</p>
 *
 * <p>Uses batch processing to handle large volumes efficiently, publishing events
 * for each batch to trigger downstream contract deactivation workflows.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractTerminationJob {

    private static final int BATCH_SIZE = 50;

    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${job.process-termination.cron:0 0 0 * * *}")
    @Transactional
    public void processTerminationDueContracts() {
        LocalDate today = LocalDate.now();
        int total = 0;
        List<Long> batch;

        do {
            batch = contractRepository.findTerminationDueContractIds(
                    today, ContractStatus.TERMINATION_DUE, PageRequest.of(0, BATCH_SIZE));
            if (batch.isEmpty()) {
                break;
            }

            contractRepository.terminateContractsByIds(batch, ContractStatus.TERMINATED);
            log.info("Terminated contract IDs: {}", batch);
            eventPublisher.publishEvent(new ContractDeactivatedEvent(batch));
            total += batch.size();
        } while (batch.size() == BATCH_SIZE);

        log.info("Terminated {} contract(s) in total", total);
    }
}

