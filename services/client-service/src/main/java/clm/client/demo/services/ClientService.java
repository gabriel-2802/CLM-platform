package clm.client.demo.services;

import clm.client.demo.dtos.request.ClientListRequest;
import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.response.ClientResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.ClientMapper;
import clm.client.demo.models.Client;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.UserClientRepository;
import clm.client.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    public List<String> listTemplateFields() {
        return Arrays.stream(ClientResponse.class.getDeclaredFields())
                .map(Field::getName)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> listClients(ClientListRequest request, Long resolvedUserIdFilter) {
        int page = Objects.nonNull(request.page()) ? request.page() : 0;
        int size = Objects.nonNull(request.size()) ? request.size() : DEFAULT_PAGE_SIZE;

        return clientRepository.findAll(buildSpecification(request, resolvedUserIdFilter), PageRequest.of(page, size))
                .map(clientMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        return clientMapper.toResponse(findClient(id));
    }

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        Client saved = clientRepository.save(clientMapper.toEntity(request));
        log.info("created client {} with id {}", saved.getName(), saved.getId());
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = findClient(id);
        clientMapper.updateEntity(client, request);
        log.info("updated client {}", id);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse partialUpdateClient(Long id, ClientRequest request) {
        Client client = findClient(id);
        clientMapper.partialUpdateEntity(client, request);
        log.info("partially updated client {}", id);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(Long id) {
        clientRepository.delete(findClient(id));
        log.info("deleted client {}", id);
    }

    private Specification<Client> buildSpecification(ClientListRequest request, Long resolvedUserIdFilter) {
        Specification<Client> spec = (root, query, builder) -> builder.conjunction();

        if (Objects.nonNull(request.active())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("active"), request.active()));
        }
        if (Objects.nonNull(request.type())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("type"), request.type()));
        }
        if (Objects.nonNull(resolvedUserIdFilter)) {
            spec = spec.and((root, query, builder) -> {
                var join = root.join("userClients", JoinType.INNER);
                query.distinct(true);
                return builder.equal(join.get("userId"), resolvedUserIdFilter);
            });
        }

        return spec;
    }

    private Client findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
    }
}