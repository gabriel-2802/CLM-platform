package clm.client.demo.services;

import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.Client;
import clm.client.demo.models.ClientHistory;
import clm.client.demo.repositories.ClientHistoryRepository;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.UserClientRepository;
import clm.client.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final ClientRepository clientRepository;
    private final ClientHistoryRepository historyRepository;
    private final UserClientRepository userClientRepository;

    @Transactional(readOnly = true)
    public List<HistoryResponse> listHistory(Long clientId) {
        enforceUserAccess(clientId);
        return historyRepository.findAllByClientId(clientId).stream()
                .map(HistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoryResponse getHistory(Long clientId, int year) {
        enforceUserAccess(clientId);
        return historyRepository.findByClientIdAndYear(clientId, year)
                .map(HistoryResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("History not found for year: " + year));
    }

    @Transactional
    public HistoryResponse upsertHistory(Long clientId, int year, HistoryRequest request) {
        Client client = findClient(clientId);
        int resolvedYear = resolveYear(year, request);
        ClientHistory history = historyRepository.findByClientIdAndYear(clientId, resolvedYear)
                .orElseGet(ClientHistory::new);

        history.setClient(client);
        history.setYear(resolvedYear);
        history.setTurnover(request.cifraAfaceri());
        history.setInventory(Boolean.TRUE.equals(request.inventar()));
        history.setJuneSemesterBalance(request.bilantSemIun());
        history.setAnnualBalance(request.bilantAnual());

        ClientHistory saved = historyRepository.save(history);
        log.info("Upserted history {} for client {}", resolvedYear, clientId);
        return HistoryResponse.from(saved);
    }

    @Transactional
    public void deleteHistory(Long clientId, int year) {
        ClientHistory history = historyRepository.findByClientIdAndYear(clientId, year)
                .orElseThrow(() -> new ResourceNotFoundException("History not found for year: " + year));
        historyRepository.delete(history);
        log.info("Deleted history {} for client {}", year, clientId);
    }

    private int resolveYear(int year, HistoryRequest request) {
        if (Objects.nonNull(request.anul()) && request.anul() != year) {
            throw new IllegalArgumentException("Request year does not match path year");
        }
        return Objects.nonNull(request.anul()) ? request.anul() : year;
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }

    private void enforceUserAccess(Long clientId) {
        if (!SecurityUtils.isUserOnly()) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Missing authenticated user id"));
        if (!userClientRepository.existsByClientIdAndUserId(clientId, userId)) {
            throw new AccessDeniedException("Client access denied");
        }
    }
}
