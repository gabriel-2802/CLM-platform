package clm.demo.controllers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.services.ReportService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts/report")
public class ReportController {

    private final ReportService reportService;

    /**
     * Returns ACTIVE contracts expiring within the next {@code days} days.
     * Consumed by the notification microservice to build expiry-alert digests.
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<ContractResponseDTO>> getExpiringContracts(
            @RequestParam @Min(1) int days) {

        List<ContractResponseDTO> result = reportService.getExpiringContracts(days);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }

    /**
     * Returns ACTIVE contracts whose clients have had no renegotiation for at least {@code months} months.
     * Consumed by the notification microservice to build inactive-client digests.
     */
    @GetMapping("/inactive-clients")
    public ResponseEntity<List<ContractResponseDTO>> getInactiveClientContracts(
            @RequestParam @Min(1) int months) {

        List<ContractResponseDTO> result = reportService.getInactiveClientContracts(months);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }
}
