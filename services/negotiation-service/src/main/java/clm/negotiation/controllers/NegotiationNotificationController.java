package clm.negotiation.controllers;

import clm.negotiation.services.NegotiationNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/negotiations/notifications")
@RequiredArgsConstructor
public class NegotiationNotificationController {

    private final NegotiationNotificationService notificationService;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger() {
        notificationService.sendInactiveClientsDigest();
        return ResponseEntity.ok(Map.of("status", "inactive clients digest triggered"));
    }
}
