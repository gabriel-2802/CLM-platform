package jobs;

import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractArchiveJob {
    private final ContractRepository contractRepository;

    @Scheduled(cron = "${job.archive-contracts.cron:0 0 0 * * *}")
    public void archiveExpiredContracts() {
        int count = contractRepository.archiveExpiredContracts(
                ContractStatus.ARCHIVED,
                ContractStatus.ACTIVE,
                LocalDate.now()
        );
        log.info("Archived {} expired contracts", count);
    }
}
