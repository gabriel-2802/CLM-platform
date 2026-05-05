package clm.client.demo.services;

import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.DetailsMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.ClientDetails;
import clm.client.demo.repositories.ClientDetailsRepository;
import clm.client.demo.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetailsService {

    private final ClientRepository clientRepository;
    private final ClientDetailsRepository detailsRepository;
    private final DetailsMapper detailsMapper;

    @Transactional(readOnly = true)
    public DetailsResponse getDetails(Long clientId) {
        return detailsRepository.findByClientId(clientId)
                .map(detailsMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Details not found for client: " + clientId));
    }

    @Transactional
    public DetailsResponse upsertDetails(Long clientId, DetailsRequest request) {
        Client client = findClient(clientId);
        ClientDetails details = detailsRepository.findByClientId(clientId).orElseGet(ClientDetails::new);
        details.setClient(client);
        detailsMapper.updateEntity(details, request);
        ClientDetails saved = detailsRepository.save(details);
        log.info("upserted details for client {}", clientId);
        return detailsMapper.toResponse(saved);
    }

    @Transactional
    public DetailsResponse patchDetails(Long clientId, DetailsRequest request) {
        ClientDetails details = detailsRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Details not found for client: " + clientId));
        detailsMapper.partialUpdateEntity(details, request);
        ClientDetails saved = detailsRepository.save(details);
        log.info("patched details for client {}", clientId);
        return detailsMapper.toResponse(saved);
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }
}