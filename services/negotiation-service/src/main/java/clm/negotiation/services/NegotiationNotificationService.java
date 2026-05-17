package clm.negotiation.services;

import clm.negotiation.dto.reports.ContractSummaryInfo;
import clm.negotiation.dto.reports.InactiveClientReport;
import clm.negotiation.repositories.NegotiationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegotiationNotificationService {

    private final NegotiationRepository repository;
    private final ContractApiClient contractApiClient;
    private final NegotiationEmailService emailService;

    @Value("${notification.inactivity-months:6}")
    private int inactivityMonths;

    public void sendInactiveClientsDigest() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(inactivityMonths);
        List<Object[]> rows = repository.findClientsInactiveSince(cutoff);

        if (rows.isEmpty()) {
            log.info("No inactive clients found — skipping email.");
            return;
        }

        List<InactiveClientReport> reports = rows.stream().map(row -> {
            Integer clientId       = (Integer) row[0];
            LocalDateTime lastDate = (LocalDateTime) row[1];

            ContractSummaryInfo contract = contractApiClient
                    .getContractForClient(clientId)
                    .orElse(null);

            String clientName      = contract != null ? contract.clientLabel(clientId) : "Client #" + clientId;
            String contractStatus  = contract != null ? contract.getContractStatus()   : null;
            var    contractEndDate = contract != null ? contract.getContractEndDate()   : null;

            return new InactiveClientReport(clientId, clientName, lastDate, contractStatus, contractEndDate);
        }).toList();

        log.info("Sending inactive clients digest: {} clients", reports.size());
        emailService.sendInactiveClientsDigest(reports, inactivityMonths);
    }
}
