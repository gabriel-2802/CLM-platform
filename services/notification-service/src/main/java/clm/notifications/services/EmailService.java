package clm.notifications.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import java.util.List;

/**
 * Sends HTML emails to the configured recipient list.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.from}")
    private String fromAddress;

    @Value("${notification.recipients}")
    private String recipientsConfig;

    /**
     * Sends an HTML email to all configured recipients.
     *
     * @param subject  email subject line
     * @param htmlBody HTML content of the email body
     */
    public void sendHtml(String subject, String htmlBody) {
        sendHtml(subject, htmlBody, null, null);
    }

    /**
     * Sends an HTML email with an optional .ics calendar attachment.
     *
     * @param subject        email subject line
     * @param htmlBody       HTML content of the email body
     * @param icsAttachment  raw bytes of the .ics file, or null to skip
     * @param icsFileName    filename shown in the email attachment, or null to skip
     */
    public void sendHtml(String subject, String htmlBody, byte[] icsAttachment, String icsFileName) {
        List<String> recipients = parseRecipients();
        if (recipients.isEmpty()) {
            log.warn("No notification recipients configured – email not sent. " +
                     "Set notification.recipients in application.properties.");
            return;
        }

        for (String recipient : recipients) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(recipient.trim());
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                if (icsAttachment != null && icsFileName != null) {
                    helper.addAttachment(icsFileName,
                            new ByteArrayDataSource(icsAttachment, "text/calendar; charset=UTF-8; method=PUBLISH"));
                }

                mailSender.send(message);
                log.info("Notification email sent to {}{}", recipient,
                        icsAttachment != null ? " (with .ics attachment)" : "");
            } catch (MessagingException e) {
                log.error("Failed to send notification email to {}: {}", recipient, e.getMessage());
            }
        }
    }

    private List<String> parseRecipients() {
        if (recipientsConfig == null || recipientsConfig.isBlank()) {
            return List.of();
        }
        return List.of(recipientsConfig.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
