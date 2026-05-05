package clm.client.demo.services;

import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.WorkPointMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.WorkPoint;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.WorkPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkPointService {

    private final ClientRepository clientRepository;
    private final WorkPointRepository workPointRepository;
    private final WorkPointMapper workPointMapper;

    @Transactional(readOnly = true)
    public List<WorkPointResponse> listWorkPoints(Long clientId) {
        return workPointRepository.findAllByClientId(clientId).stream()
                .map(workPointMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkPointResponse getWorkPoint(Long clientId, Long id) {
        return workPointRepository.findByIdAndClientId(id, clientId)
                .map(workPointMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Work point not found: " + id));
    }

    @Transactional
    public WorkPointResponse createWorkPoint(Long clientId, WorkPointRequest request) {
        Client client = findClient(clientId);
        WorkPoint workPoint = workPointMapper.toEntity(request);
        workPoint.setClient(client);
        WorkPoint saved = workPointRepository.save(workPoint);
        log.info("created work point {} for client {}", saved.getId(), clientId);
        return workPointMapper.toResponse(saved);
    }

    @Transactional
    public WorkPointResponse updateWorkPoint(Long clientId, Long id, WorkPointRequest request) {
        WorkPoint workPoint = workPointRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Work point not found: " + id));
        workPointMapper.updateEntity(workPoint, request);
        WorkPoint saved = workPointRepository.save(workPoint);
        log.info("updated work point {}", saved.getId());
        return workPointMapper.toResponse(saved);
    }

    @Transactional
    public void deleteWorkPoint(Long clientId, Long id) {
        WorkPoint workPoint = workPointRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Work point not found: " + id));
        workPointRepository.delete(workPoint);
        log.info("deleted work point {}", id);
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }
}