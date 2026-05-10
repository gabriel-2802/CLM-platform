package clm.negotiation.jobs;

import clm.negotiation.services.NegotiationNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NegotiationNotificationJob {

    private final NegotiationNotificationService notificationService;

    @Scheduled(cron = "${notification.cron}")
    public void runInactiveClientsDigest() {
        log.info("NegotiationNotificationJob triggered — sending inactive clients digest");
        try {
            notificationService.sendInactiveClientsDigest();
        } catch (Exception e) {
            log.error("NegotiationNotificationJob failed: {}", e.getMessage(), e);
        }
    }
}
