package clm.negotiation.services;

import clm.negotiation.dto.requests.ContractActivatedRequest;
import clm.negotiation.dto.requests.ContractDeactivatedRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.mappers.NegotiationMapper;
import clm.negotiation.models.Negotiation;
import clm.negotiation.models.TerminatedContract;
import clm.negotiation.models.enums.NegotiationStatus;
import clm.negotiation.repositories.NegotiationRepository;
import clm.negotiation.repositories.TerminatedContractRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ContractLifecycleService {

    private final NegotiationRepository        negotiationRepository;
    private final TerminatedContractRepository terminatedContractRepository;
    private final NegotiationMapper            mapper;

    @Transactional
    public NegotiationResponseDTO onContractActivated(@Valid ContractActivatedRequest request) {
        LocalDate effectiveEndDate = request.endDate() != null ? request.endDate() : request.startDate();

        Negotiation initial = Negotiation.builder()
                .contractId(request.contractId())
                .clientId(request.clientId())
                .proposedValue(request.contractValue())
                .proposedEndDate(effectiveEndDate)
                .status(NegotiationStatus.ACCEPTED)
                .notes("Negociere inițială — contract semnat")
                .createdByUserId(0)
                .createdAt(request.startDate().atStartOfDay())
                .build();

        Negotiation saved = negotiationRepository.save(initial);
        log.info("Created initial negotiation {} for contract {} (client {})",
                saved.getId(), request.contractId(), request.clientId());
        return mapper.toDto(saved);
    }

    @Transactional
    public void onContractsDeactivated(@Valid ContractDeactivatedRequest request) {
        for (Long contractId : request.contractIds()) {
            if (!terminatedContractRepository.existsByContractId(contractId)) {
                terminatedContractRepository.save(
                        TerminatedContract.builder().contractId(contractId).build());
                log.info("Contract {} marked as terminated — excluded from inactivity emails", contractId);
            } else {
                log.debug("Contract {} already marked as terminated — skipping", contractId);
            }
        }
    }
}
