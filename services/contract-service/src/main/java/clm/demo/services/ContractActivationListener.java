package clm.demo.services;

import clm.demo.events.ContractActivatedEvent;
import clm.negotiation.dto.requests.ContractActivatedRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractActivationListener {

    private final NegotiationLifecycleClient negotiationLifecycleClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContractActivated(ContractActivatedEvent event) {
        log.info("Handling contract activation event: contractId={}, clientId={}",
                event.contractId(), event.clientId());
        ContractActivatedRequest request = new ContractActivatedRequest(
                event.contractId(),
                event.clientId(),
                event.startDate(),
                event.endDate(),
                event.contractValue()
        );
        negotiationLifecycleClient.notifyContractActivated(request);
    }
}
