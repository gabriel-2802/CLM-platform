package clm.client.demo.services;

import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.HistoryMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.ClientHistory;
import clm.client.demo.repositories.ClientHistoryRepository;
import clm.client.demo.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final HistoryMapper historyMapper;

    @Transactional(readOnly = true)
    public List<HistoryResponse> listHistory(Long clientId) {
        return historyRepository.findAllByClientId(clientId).stream()
                .map(historyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoryResponse getHistory(Long clientId, int year) {
        return historyRepository.findByClientIdAndYear(clientId, year)
                .map(historyMapper::toResponse)
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
        historyMapper.updateEntity(history, request);

        ClientHistory saved = historyRepository.save(history);
        log.info("upserted history {} for client {}", resolvedYear, clientId);
        return historyMapper.toResponse(saved);
    }

    @Transactional
    public void deleteHistory(Long clientId, int year) {
        ClientHistory history = historyRepository.findByClientIdAndYear(clientId, year)
                .orElseThrow(() -> new ResourceNotFoundException("History not found for year: " + year));
        historyRepository.delete(history);
        log.info("deleted history {} for client {}", year, clientId);
    }

    private int resolveYear(int year, HistoryRequest request) {
        if (Objects.nonNull(request.year()) && request.year() != year) {
            throw new IllegalArgumentException("Request year does not match path year");
        }
        return Objects.nonNull(request.year()) ? request.year() : year;
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }
}