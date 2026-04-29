package clm.client.demo.services;

import clm.client.demo.dtos.request.ClientListRequest;
import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.response.ClientResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ClientRepository clientRepository;
    private final UserClientRepository userClientRepository;

    @Transactional(readOnly = true)
    public Page<ClientResponse> listClients(ClientListRequest request) {
        Long userIdFilter = request.userId();

        if (Objects.nonNull(userIdFilter) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new AccessDeniedException("Filtering by userId requires ROLE_ADMIN");
        }

        if (SecurityUtils.isUserOnly()) {
            userIdFilter = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new AccessDeniedException("Missing authenticated user id"));
        }

        Long resolvedUserIdFilter = userIdFilter;
        Specification<Client> specification = (root, query, builder) -> builder.conjunction();
        if (Objects.nonNull(request.activa())) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("active"), request.activa()));
        }
        if (Objects.nonNull(request.tip())) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("type"), request.tip()));
        }
        if (Objects.nonNull(resolvedUserIdFilter)) {
            specification = specification.and((root, query, builder) -> {
                var join = root.join("userClients", JoinType.INNER);
                query.distinct(true);
                return builder.equal(join.get("userId"), resolvedUserIdFilter);
            });
        }

        int page = Objects.nonNull(request.page()) ? request.page() : 0;
        int size = Objects.nonNull(request.size()) ? request.size() : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        return clientRepository.findAll(specification, pageable).map(ClientResponse::from);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        enforceUserAccess(id);
        return ClientResponse.from(findClient(id));
    }

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        var client = new Client();
        applyFullUpdate(client, request);
        Client saved = clientRepository.save(client);
        log.info("Created client {} with id {}", saved.getName(), saved.getId());
        return ClientResponse.from(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = findClient(id);
        applyFullUpdate(client, request);
        Client saved = clientRepository.save(client);
        log.info("Updated client {}", saved.getId());
        return ClientResponse.from(saved);
    }

    @Transactional
    public ClientResponse partialUpdateClient(Long id, ClientRequest request) {
        Client client = findClient(id);
        applyPartialUpdate(client, request);
        Client saved = clientRepository.save(client);
        log.info("Partially updated client {}", saved.getId());
        return ClientResponse.from(saved);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = findClient(id);
        clientRepository.delete(client);
        log.info("Deleted client {}", id);
    }

    private Client findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
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

    private void applyFullUpdate(Client client, ClientRequest request) {
        client.setName(request.denumire());
        client.setType(request.tip());
        client.setTaxId(request.cui());
        client.setActive(Boolean.TRUE.equals(request.activa()));
        client.setVerificationDate(request.dataVerificarii());
        client.setAddress(request.adresa());
        client.setAdministration(request.administratie());
        client.setTaxType(request.impozit());
        client.setVatPayer(request.platitorTVA());
        client.setVatOnCollection(request.tvaLaIncasare());
        client.setHasEuVatCode(request.areCodTVAUE());
        client.setEuVatCode(request.codTVAUE());
        client.setEuOperation(request.operatiuneUE());
        client.setDividends(request.dividende());
        client.setEmployees(request.salariati());
        client.setCashRegister(request.casaDeMarcat());
        client.setHqExpirationDate(request.dataExpSediuSocial());
        client.setAdminMandateExpiration(request.dataExpMandatAdmin());
        client.setFiscalCertificateDate(request.dataCertificatFiscal());
        client.setPayerSheetDate(request.dataFisaPlatitor());
        client.setFiscalVectorDate(request.dataVectFiscal());
    }

    private void applyPartialUpdate(Client client, ClientRequest request) {
        if (Objects.nonNull(request.denumire())) client.setName(request.denumire());
        if (Objects.nonNull(request.tip())) client.setType(request.tip());
        if (Objects.nonNull(request.cui())) client.setTaxId(request.cui());
        if (Objects.nonNull(request.activa())) client.setActive(request.activa());
        if (Objects.nonNull(request.dataVerificarii())) client.setVerificationDate(request.dataVerificarii());
        if (Objects.nonNull(request.adresa())) client.setAddress(request.adresa());
        if (Objects.nonNull(request.administratie())) client.setAdministration(request.administratie());
        if (Objects.nonNull(request.impozit())) client.setTaxType(request.impozit());
        if (Objects.nonNull(request.platitorTVA())) client.setVatPayer(request.platitorTVA());
        if (Objects.nonNull(request.tvaLaIncasare())) client.setVatOnCollection(request.tvaLaIncasare());
        if (Objects.nonNull(request.areCodTVAUE())) client.setHasEuVatCode(request.areCodTVAUE());
        if (Objects.nonNull(request.codTVAUE())) client.setEuVatCode(request.codTVAUE());
        if (Objects.nonNull(request.operatiuneUE())) client.setEuOperation(request.operatiuneUE());
        if (Objects.nonNull(request.dividende())) client.setDividends(request.dividende());
        if (Objects.nonNull(request.salariati())) client.setEmployees(request.salariati());
        if (Objects.nonNull(request.casaDeMarcat())) client.setCashRegister(request.casaDeMarcat());
        if (Objects.nonNull(request.dataExpSediuSocial())) client.setHqExpirationDate(request.dataExpSediuSocial());
        if (Objects.nonNull(request.dataExpMandatAdmin())) client.setAdminMandateExpiration(request.dataExpMandatAdmin());
        if (Objects.nonNull(request.dataCertificatFiscal())) client.setFiscalCertificateDate(request.dataCertificatFiscal());
        if (Objects.nonNull(request.dataFisaPlatitor())) client.setPayerSheetDate(request.dataFisaPlatitor());
        if (Objects.nonNull(request.dataVectFiscal())) client.setFiscalVectorDate(request.dataVectFiscal());
    }
}
