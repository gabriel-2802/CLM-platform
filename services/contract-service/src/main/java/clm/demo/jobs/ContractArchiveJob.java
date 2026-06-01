package clm.demo.jobs;

import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import clm.demo.events.ContractDeactivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractArchiveJob {

    private static final int BATCH_SIZE = 50;

    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${job.archive-contracts.cron:0 0 0 * * *}")
    @Transactional
    public void archiveExpiredContracts() {
        LocalDate today = LocalDate.now();
        int total = 0;
        List<Long> batch;

        do {
            batch = contractRepository.findExpiredContractIds(today, ContractStatus.ACTIVE, PageRequest.of(0, BATCH_SIZE));
            if (batch.isEmpty()) {
                break;
            }

            contractRepository.archiveContractsByIds(batch, ContractStatus.ARCHIVED);
            log.info("Archived contract IDs: {}", batch);
            eventPublisher.publishEvent(new ContractDeactivatedEvent(batch));
            total += batch.size();
        } while (batch.size() == BATCH_SIZE);

        log.info("Archived {} expired contract(s) in total", total);
    }
}
