package clm.client.demo.services;

import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.Client;
import clm.client.demo.models.WorkPoint;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.UserClientRepository;
import clm.client.demo.repositories.WorkPointRepository;
import clm.client.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkPointService {

    private final ClientRepository clientRepository;
    private final WorkPointRepository workPointRepository;
    private final UserClientRepository userClientRepository;

    @Transactional(readOnly = true)
    public List<WorkPointResponse> listWorkPoints(Long clientId) {
        enforceUserAccess(clientId);
        return workPointRepository.findAllByClientId(clientId).stream()
                .map(WorkPointResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkPointResponse getWorkPoint(Long clientId, Long id) {
        enforceUserAccess(clientId);
        return workPointRepository.findByIdAndClientId(id, clientId)
                .map(WorkPointResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Work point not found: " + id));
    }

    @Transactional
    public WorkPointResponse createWorkPoint(Long clientId, WorkPointRequest request) {
        Client client = findClient(clientId);
        var workPoint = new WorkPoint();
        workPoint.setClient(client);
        applyFullUpdate(workPoint, request);
        WorkPoint saved = workPointRepository.save(workPoint);
        log.info("Created work point {} for client {}", saved.getId(), clientId);
        return WorkPointResponse.from(saved);
    }

    @Transactional
    public WorkPointResponse updateWorkPoint(Long clientId, Long id, WorkPointRequest request) {
        WorkPoint workPoint = workPointRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Work point not found: " + id));
        applyFullUpdate(workPoint, request);
        WorkPoint saved = workPointRepository.save(workPoint);
        log.info("Updated work point {}", saved.getId());
        return WorkPointResponse.from(saved);
    }

    @Transactional
    public void deleteWorkPoint(Long clientId, Long id) {
        WorkPoint workPoint = workPointRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Work point not found: " + id));
        workPointRepository.delete(workPoint);
        log.info("Deleted work point {}", id);
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

    private void applyFullUpdate(WorkPoint workPoint, WorkPointRequest request) {
        workPoint.setName(request.denumire());
        workPoint.setValidFrom(request.deLa());
        workPoint.setValidTo(request.panaLa());
        workPoint.setAdministration(request.administratie());
        workPoint.setUcRegistry(Boolean.TRUE.equals(request.registruUC()));
        workPoint.setEmployeeCount(request.salariati());
        workPoint.setTaxId(request.cui());
        workPoint.setCashRegister(Boolean.TRUE.equals(request.casaDeMarcat()));
    }
}
