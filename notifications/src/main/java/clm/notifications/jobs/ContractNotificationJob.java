package clm.notifications.jobs;

import clm.notifications.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that triggers the monthly contract notification digest.
 *
 * <p>Default cron: {@code 0 0 8 1 * *} – 08:00 on the 1st day of each month.
 * Override via the {@code NOTIFICATION_CRON} environment variable or the
 * {@code notification.cron} property.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractNotificationJob {

    private final NotificationService notificationService;

    @Scheduled(cron = "${notification.cron}")
    public void runMonthlyDigest() {
        log.info("ContractNotificationJob triggered – sending monthly digest");
        try {
            notificationService.sendMonthlyDigest();
        } catch (Exception e) {
            log.error("ContractNotificationJob failed: {}", e.getMessage(), e);
        }
    }
}
