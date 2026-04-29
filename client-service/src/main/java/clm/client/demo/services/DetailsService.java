package clm.client.demo.services;

import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.Client;
import clm.client.demo.models.ClientDetails;
import clm.client.demo.repositories.ClientDetailsRepository;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.UserClientRepository;
import clm.client.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetailsService {

    private final ClientRepository clientRepository;
    private final ClientDetailsRepository detailsRepository;
    private final UserClientRepository userClientRepository;

    @Transactional(readOnly = true)
    public DetailsResponse getDetails(Long clientId) {
        enforceUserAccess(clientId);
        return detailsRepository.findByClientId(clientId)
                .map(DetailsResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Details not found for client: " + clientId));
    }

    @Transactional
    public DetailsResponse upsertDetails(Long clientId, DetailsRequest request) {
        Client client = findClient(clientId);
        ClientDetails details = detailsRepository.findByClientId(clientId).orElseGet(ClientDetails::new);

        details.setClient(client);
        applyFullUpdate(details, request);
        ClientDetails saved = detailsRepository.save(details);
        log.info("Upserted details for client {}", clientId);
        return DetailsResponse.from(saved);
    }

    @Transactional
    public DetailsResponse patchDetails(Long clientId, DetailsRequest request) {
        ClientDetails details = detailsRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Details not found for client: " + clientId));
        applyPartialUpdate(details, request);
        ClientDetails saved = detailsRepository.save(details);
        log.info("Patched details for client {}", clientId);
        return DetailsResponse.from(saved);
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

    private void applyFullUpdate(ClientDetails details, DetailsRequest request) {
        details.setUcRegistry(request.registruUC());
        details.setFiscalEvidenceRegistry(request.registruEvFiscala());
        details.setMoneyLaunderingOffice(request.ofSpalareBani());
        details.setInternalRules(request.regulamentOrdineInterioara());
        details.setAccountingPoliciesManual(request.manualPoliticiContabile());
        details.setRevisalAddress(request.adresaRevisal());
        details.setItmPassword(request.parolaITM());
        details.setOnlineDeclarations(request.depunereDeclaratiiOnline());
        details.setFiscalFileAccess(request.accesDosarFiscal());
    }

    private void applyPartialUpdate(ClientDetails details, DetailsRequest request) {
        if (Objects.nonNull(request.registruUC())) details.setUcRegistry(request.registruUC());
        if (Objects.nonNull(request.registruEvFiscala())) details.setFiscalEvidenceRegistry(request.registruEvFiscala());
        if (Objects.nonNull(request.ofSpalareBani())) details.setMoneyLaunderingOffice(request.ofSpalareBani());
        if (Objects.nonNull(request.regulamentOrdineInterioara())) details.setInternalRules(request.regulamentOrdineInterioara());
        if (Objects.nonNull(request.manualPoliticiContabile())) details.setAccountingPoliciesManual(request.manualPoliticiContabile());
        if (Objects.nonNull(request.adresaRevisal())) details.setRevisalAddress(request.adresaRevisal());
        if (Objects.nonNull(request.parolaITM())) details.setItmPassword(request.parolaITM());
        if (Objects.nonNull(request.depunereDeclaratiiOnline())) details.setOnlineDeclarations(request.depunereDeclaratiiOnline());
        if (Objects.nonNull(request.accesDosarFiscal())) details.setFiscalFileAccess(request.accesDosarFiscal());
    }
}
