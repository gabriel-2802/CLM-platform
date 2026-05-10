package clm.negotiation.services;

import clm.negotiation.dto.requests.CreateNegotiationRequest;
import clm.negotiation.dto.requests.UpdateNotesRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.exceptions.exceptions.InvalidNegotiationStateException;
import clm.negotiation.exceptions.exceptions.ResourceNotFoundException;
import clm.negotiation.mappers.NegotiationMapper;
import clm.negotiation.models.Negotiation;
import clm.negotiation.models.enums.NegotiationStatus;
import clm.negotiation.repositories.NegotiationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class NegotiationService {

    private final NegotiationRepository repository;
    private final NegotiationMapper     mapper;
    private final ContractApiClient     contractApiClient;

    @Transactional
    public NegotiationResponseDTO create(@Valid CreateNegotiationRequest request) {
        contractApiClient.requireContractActive(request.contractId());
        Negotiation negotiation = Negotiation.builder()
                .contractId(request.contractId())
                .clientId(request.clientId())
                .proposedValue(request.proposedValue())
                .proposedEndDate(request.proposedEndDate())
                .createdByUserId(request.createdByUserId())
                .createdByUserName(request.createdByUserName())
                .notes(request.notes())
                .status(NegotiationStatus.DRAFT)
                .build();

        Negotiation saved = repository.save(negotiation);
        log.info("Created negotiation {} for contract {}", saved.getId(), saved.getContractId());
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public NegotiationResponseDTO getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<NegotiationResponseDTO> getByContractId(Long contractId) {
        return repository.findByContractIdOrderByCreatedAtDesc(contractId)
                .stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NegotiationResponseDTO> getByClientId(Integer clientId) {
        return repository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream().map(mapper::toDto).toList();
    }

    @Transactional
    public NegotiationResponseDTO send(Long id) {
        Negotiation n = findOrThrow(id);
        requireStatus(n, NegotiationStatus.DRAFT, "Only DRAFT negotiations can be sent");
        n.setStatus(NegotiationStatus.SENT);
        log.info("Negotiation {} sent to client", id);
        return mapper.toDto(repository.save(n));
    }

    @Transactional
    public NegotiationResponseDTO accept(Long id) {
        Negotiation n = findOrThrow(id);
        requireStatus(n, NegotiationStatus.SENT, "Only SENT negotiations can be accepted");
        n.setStatus(NegotiationStatus.ACCEPTED);
        n.setResolvedAt(LocalDateTime.now());
        repository.save(n);

        contractApiClient.applyRenegotiation(n.getContractId(), n.getProposedValue(), n.getProposedEndDate());
        log.info("Negotiation {} accepted — contract {} updated", id, n.getContractId());
        return mapper.toDto(n);
    }

    @Transactional
    public NegotiationResponseDTO reject(Long id, UpdateNotesRequest request) {
        Negotiation n = findOrThrow(id);
        requireStatus(n, NegotiationStatus.SENT, "Only SENT negotiations can be rejected");
        n.setStatus(NegotiationStatus.REJECTED);
        n.setResolvedAt(LocalDateTime.now());
        if (request != null && request.notes() != null) {
            n.setNotes(request.notes());
        }
        log.info("Negotiation {} rejected", id);
        return mapper.toDto(repository.save(n));
    }

    @Transactional
    public NegotiationResponseDTO updateNotes(Long id, @Valid UpdateNotesRequest request) {
        Negotiation n = findOrThrow(id);
        n.setNotes(request.notes());
        return mapper.toDto(repository.save(n));
    }

    private Negotiation findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation not found: " + id));
    }

    private void requireStatus(Negotiation n, NegotiationStatus required, String message) {
        if (n.getStatus() != required) {
            throw new InvalidNegotiationStateException(
                    message + ". Current status: " + n.getStatus());
        }
    }
}
