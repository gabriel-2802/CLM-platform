package clm.demo.services;

import clm.demo.events.ContractDeactivatedEvent;
import clm.negotiation.dto.requests.ContractDeactivatedRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractDeactivationListener {

    private final NegotiationLifecycleClient negotiationLifecycleClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContractDeactivated(ContractDeactivatedEvent event) {
        log.info("Handling contract deactivation event for {} contract(s)",
                event.contractIds().size());
        ContractDeactivatedRequest request = new ContractDeactivatedRequest(event.contractIds());
        negotiationLifecycleClient.notifyContractsDeactivated(request);
    }
}
