package clm.notifications.controllers;

import clm.notifications.dto.ContractSummaryDTO;
import clm.notifications.services.ContractApiService;
import clm.notifications.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Exposes endpoints useful for testing and manual triggering.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ContractApiService contractApiService;

    /**
     * Triggers the monthly digest immediately (for testing).
     * POST /notifications/trigger
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger() {
        notificationService.sendMonthlyDigest();
        return ResponseEntity.ok(Map.of("status", "digest triggered"));
    }

    /**
     * Preview the data that would be included in the next digest.
     * GET /notifications/preview
     */
    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview() {
        List<ContractSummaryDTO> expiring = contractApiService.fetchExpiringContracts();
        return ResponseEntity.ok(Map.of("expiringContracts", expiring));
    }
}
