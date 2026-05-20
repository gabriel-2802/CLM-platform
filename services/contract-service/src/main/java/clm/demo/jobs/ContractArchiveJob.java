package clm.demo.jobs;

import clm.demo.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractArchiveJob {

    private final ContractRepository contractRepository;

    @Scheduled(cron = "${job.archive-contracts.cron:0 0 0 * * *}")
    public void archiveExpiredContracts() {
        int count = contractRepository.archiveExpiredContracts(LocalDate.now());
        log.info("Archived {} expired contract(s)", count);
    }
}
