package clm.notifications.services;

import clm.notifications.dto.ContractSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

/**
 * Communicates with the contracts microservice to fetch contract data
 * needed for the monthly notification digest.
 *
 * <p>All calls are read-only GET requests to the contracts API.
 * The service returns empty lists on any error so the notification job
 * can still send a partial digest instead of failing completely.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractApiService {

    private final RestClient contractsRestClient;

    @Value("${notification.expiry-warning-days:30}")
    private int expiryWarningDays;

    @Value("${notification.inactivity-months:6}")
    private int inactivityMonths;

    /**
     * Fetches ACTIVE contracts expiring within the configured look-ahead window.
     *
     * @return list of expiring contracts, or empty list on error / no results
     */
    public List<ContractSummaryDTO> fetchExpiringContracts() {
        try {
            List<ContractSummaryDTO> result = contractsRestClient.get()
                    .uri("/api/contracts/report/expiring?days={days}", expiryWarningDays)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Contracts API returned {} for expiring contracts endpoint", res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (result == null) return Collections.emptyList();
            log.info("Fetched {} expiring contracts from contracts API", result.size());
            return result;

        } catch (RestClientException e) {
            log.error("Failed to fetch expiring contracts from contracts API: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches the most recent contract for each client not renegotiated
     * within the configured inactivity window.
     *
     * @return list of stale client contracts, or empty list on error / no results
     */
    public List<ContractSummaryDTO> fetchInactiveClientContracts() {
        try {
            List<ContractSummaryDTO> result = contractsRestClient.get()
                    .uri("/api/contracts/report/inactive-clients?months={months}", inactivityMonths)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Contracts API returned {} for inactive-clients endpoint", res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (result == null) return Collections.emptyList();
            log.info("Fetched {} inactive client contracts from contracts API", result.size());
            return result;

        } catch (RestClientException e) {
            log.error("Failed to fetch inactive client contracts from contracts API: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
