package clm.demo.services;

import clm.negotiation.dto.requests.ContractActivatedRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import clm.demo.security.JwtTokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class NegotiationLifecycleClient {

    private final RestClient negotiationRestClient;
    private final JwtTokenProvider jwtTokenProvider;

    public void notifyContractActivated(ContractActivatedRequest request) {
        log.info("Sending contract activation to negotiation-service for contractId={}", request.contractId());
        log.debug("Activation payload: clientId={}, startDate={}, endDate={}, value={}",
                request.clientId(), request.startDate(), request.endDate(), request.contractValue());
        try {
                String token = jwtTokenProvider.generateServiceToken();
                var response = negotiationRestClient.post()
                    .uri("/api/negotiations/contracts/activated")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Negotiation-service activation accepted for contractId={} (status={})",
                    request.contractId(), response.getStatusCode());
        } catch (RestClientException e) {
            log.warn("Failed to notify negotiation-service for contract {}: {}",
                    request.contractId(), e.getMessage());
        }
    }
}
