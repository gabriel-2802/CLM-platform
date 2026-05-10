package clm.negotiation.services;

import clm.negotiation.dto.reports.InactiveClientReport;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegotiationEmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.from}")
    private String fromAddress;

    @Value("${notification.recipients}")
    private String recipientsConfig;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void sendInactiveClientsDigest(List<InactiveClientReport> inactiveClients, int inactivityMonths) {
        List<String> recipients = parseRecipients();
        if (recipients.isEmpty()) {
            log.warn("No recipients configured for negotiation notifications — email not sent.");
            return;
        }

        String subject = "CLM Platform – Clienți fără negocieri (" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MM.yyyy")) + ")";
        String body = buildHtmlBody(inactiveClients, inactivityMonths);

        for (String recipient : recipients) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(recipient.trim());
                helper.setSubject(subject);
                helper.setText(body, true);
                mailSender.send(message);
                log.info("Inactive clients digest sent to {}", recipient);
            } catch (MessagingException e) {
                log.error("Failed to send inactive clients digest to {}: {}", recipient, e.getMessage());
            }
        }
    }

    private String buildHtmlBody(List<InactiveClientReport> clients, int inactivityMonths) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html>
            <html lang="ro">
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif; color: #333; font-size: 14px; }
                h1   { color: #1a56db; font-size: 20px; }
                h2   { color: #374151; font-size: 16px; border-bottom: 1px solid #e5e7eb; padding-bottom: 4px; }
                table { border-collapse: collapse; width: 100%; margin-top: 8px; }
                th   { background: #f3f4f6; text-align: left; padding: 8px 12px; font-size: 13px; }
                td   { padding: 7px 12px; border-bottom: 1px solid #f3f4f6; font-size: 13px; }
                tr:hover td { background: #f9fafb; }
                .badge-stale { background:#fee2e2; color:#991b1b; padding:2px 8px; border-radius:4px; font-size:12px; }
                .empty { color:#9ca3af; font-style:italic; }
                .footer { margin-top:32px; font-size:11px; color:#9ca3af; }
              </style>
            </head>
            <body>
            """);

        sb.append("<h1>Clienți fără negocieri recente – CLM Platform</h1>");
        sb.append("<p>Generat automat pe <strong>")
          .append(LocalDate.now().format(DATE_FMT))
          .append("</strong></p>");

        sb.append("<h2>&#x1F4C5; Clienți cu care nu s-a negociat în ultimele ")
          .append(inactivityMonths)
          .append(" luni</h2>");

        if (clients.isEmpty()) {
            sb.append("<p class=\"empty\">Toți clienții au avut activitate recentă de negociere.</p>");
        } else {
            sb.append("""
                <table>
                  <thead>
                    <tr>
                      <th>Client</th>
                      <th>Ultima negociere</th>
                      <th>Status contract</th>
                      <th>Expirare contract</th>
                    </tr>
                  </thead>
                  <tbody>
                """);
            for (InactiveClientReport c : clients) {
                String lastDate = c.getLastNegotiationAt() != null
                        ? c.getLastNegotiationAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        : "—";
                String endDate = c.getContractEndDate() != null
                        ? c.getContractEndDate().format(DATE_FMT)
                        : "—";
                sb.append("<tr>")
                  .append("<td>").append(escapeHtml(c.getClientName())).append("</td>")
                  .append("<td><span class=\"badge-stale\">").append(lastDate).append("</span></td>")
                  .append("<td>").append(escapeHtml(orDash(c.getContractStatus()))).append("</td>")
                  .append("<td>").append(endDate).append("</td>")
                  .append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        sb.append("""
            <div class="footer">
              Acest email a fost generat automat de CLM Platform – Negotiation Service.<br/>
              Nu răspundeți la acest email.
            </div>
            </body></html>
            """);

        return sb.toString();
    }

    private List<String> parseRecipients() {
        if (recipientsConfig == null || recipientsConfig.isBlank()) return List.of();
        return List.of(recipientsConfig.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String escapeHtml(String text) {
        if (text == null) return "—";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String orDash(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }
}
