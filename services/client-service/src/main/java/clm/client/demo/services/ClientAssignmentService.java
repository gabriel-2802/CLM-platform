package clm.client.demo.services;

import clm.client.demo.dtos.request.AssignmentRequest;
import clm.client.demo.dtos.response.AssignmentResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.Client;
import clm.client.demo.models.UserClient;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.UserClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAssignmentService {

    private final ClientRepository clientRepository;
    private final UserClientRepository userClientRepository;

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignedUsers(Long clientId) {
        ensureClientExists(clientId);
        List<Long> userIds = userClientRepository.findAllByClientId(clientId).stream()
                .map(UserClient::getUserId)
                .toList();
        return new AssignmentResponse(clientId, userIds);
    }

    @Transactional
    public AssignmentResponse replaceAssignments(Long clientId, AssignmentRequest request) {
        Client client = findClient(clientId);
        userClientRepository.deleteByClientId(clientId);

        Set<Long> distinctUserIds = request.userIds().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        userClientRepository.saveAll(
                distinctUserIds.stream()
                        .map(userId -> buildAssignment(client, userId))
                        .toList()
        );

        log.info("replaced assignments for client {} with {} users", clientId, distinctUserIds.size());
        return new AssignmentResponse(clientId, List.copyOf(distinctUserIds));
    }

    @Transactional
    public void assignUser(Long clientId, Long userId) {
        Client client = findClient(clientId);
        try {
            userClientRepository.save(buildAssignment(client, userId));
            log.info("assigned user {} to client {}", userId, clientId);
        } catch (DataIntegrityViolationException ignored) {
            log.debug("user {} already assigned to client {} — skipping duplicate", userId, clientId);
        }
    }

    @Transactional
    public void removeUser(Long clientId, Long userId) {
        ensureClientExists(clientId);
        userClientRepository.deleteByClientIdAndUserId(clientId, userId);
        log.info("removed user {} from client {}", userId, clientId);
    }

    @Transactional(readOnly = true)
    public List<Long> getClientsForUser(Long userId) {
        return userClientRepository.findAllByUserId(userId).stream()
                .map(uc -> uc.getClient().getId())
                .toList();
    }

    private UserClient buildAssignment(Client client, Long userId) {
        var assignment = new UserClient();
        assignment.setClient(client);
        assignment.setUserId(userId);
        return assignment;
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }

    private void ensureClientExists(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found: " + clientId);
        }
    }
}