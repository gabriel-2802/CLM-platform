package clm.negotiation.services;

import clm.negotiation.dto.reports.ContractSummaryInfo;
import clm.negotiation.exceptions.exceptions.InvalidNegotiationStateException;
import clm.negotiation.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ContractApiClient {

    private final RestClient restClient;
    private final JwtTokenProvider jwtTokenProvider;

    public ContractApiClient(@Value("${contracts.service.url}") String contractsBaseUrl,
                             JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.restClient = RestClient.builder().baseUrl(contractsBaseUrl).build();
    }

    public void requireContractActive(Long contractId) {
        String token = jwtTokenProvider.generateServiceToken();
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/api/contracts/{id}", contractId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            String status = response != null ? (String) response.get("contractStatus") : null;
            if (!"ACTIVE".equals(status)) {
                throw new InvalidNegotiationStateException(
                        "Negocierile pot fi inițiate doar pentru contracte ACTIVE. Status curent: " + status);
            }
        } catch (InvalidNegotiationStateException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            throw new InvalidNegotiationStateException("Contractul nu a fost găsit sau nu este accesibil.");
        } catch (Exception e) {
            log.error("Failed to check contract status for contract {}: {}", contractId, e.getMessage());
            throw new RuntimeException("Could not verify contract status: " + e.getMessage(), e);
        }
    }

    public void applyRenegotiation(Long contractId, BigDecimal newValue, LocalDate newEndDate) {
        Map<String, Object> body = new HashMap<>();
        if (newValue   != null) body.put("contractValue",   newValue);
        if (newEndDate != null) body.put("contractEndDate", newEndDate.toString());

        String token = jwtTokenProvider.generateServiceToken();

        try {
            restClient.patch()
                    .uri("/api/contracts/{id}/renegotiate", contractId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Applied renegotiation to contract {}", contractId);
        } catch (HttpClientErrorException e) {  // 4xx from contract-service
            log.error("Contract service rejected renegotiation for contract {}: {} {}", contractId, e.getStatusCode(), e.getMessage());
            throw new InvalidNegotiationStateException(
                    "Contractul nu poate fi renegociat (status contract: " + e.getStatusCode() + "). " + extractDetail(e));
        } catch (Exception e) {
            log.error("Failed to apply renegotiation to contract {}: {}", contractId, e.getMessage());
            throw new RuntimeException("Could not update contract " + contractId + ": " + e.getMessage(), e);
        }
    }

    public Optional<ContractSummaryInfo> getContractForClient(Integer clientId) {
        String token = jwtTokenProvider.generateServiceToken();
        Map<String, Object> searchRequest = Map.of("clientId", clientId, "page", 0, "size", 1);
        try {
            List<ContractSummaryInfo> results = restClient.post()
                    .uri("/api/contracts/search")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(searchRequest)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (results == null || results.isEmpty()) return Optional.empty();
            return Optional.of(results.get(0));
        } catch (Exception e) {
            log.warn("Could not fetch contract for client {}: {}", clientId, e.getMessage());
            return Optional.empty();
        }
    }

    private String extractDetail(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            int idx = body.indexOf("\"detail\":");
            if (idx < 0) return "";
            int start = body.indexOf('"', idx + 9) + 1;
            int end   = body.indexOf('"', start);
            return start > 0 && end > start ? body.substring(start, end) : "";
        } catch (Exception ignored) {
            return "";
        }
    }
}
