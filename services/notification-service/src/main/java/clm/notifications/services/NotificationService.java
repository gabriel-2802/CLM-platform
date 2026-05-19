package clm.notifications.services;

import clm.notifications.dto.ContractSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Orchestrates the monthly notification digest:
 * <ol>
 *   <li>Fetches expiring contracts and inactive clients from the contracts API</li>
 *   <li>Builds an HTML email body</li>
 *   <li>Delegates sending to {@link EmailService}</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ContractApiService contractApiService;
    private final EmailService emailService;
    private final ICalendarService iCalendarService;

    @Value("${notification.expiry-warning-days:30}")
    private int expiryWarningDays;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Fetches both data sets, composes the HTML digest and sends the email.
     * Skips sending when both lists are empty.
     */
    public void sendMonthlyDigest() {
        log.info("Starting contract expiry notification digest...");

        List<ContractSummaryDTO> expiring = contractApiService.fetchExpiringContracts();

        if (expiring.isEmpty()) {
            log.info("No expiring contracts found – skipping email.");
            return;
        }

        String subject = "CLM Platform – Contracte care expiră curând (" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MM.yyyy")) + ")";
        String body = buildHtmlBody(expiring);

        byte[] icsBytes = iCalendarService.buildCalendarFile(expiring);
        String icsFileName = "contracte-expirare-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) + ".ics";

        emailService.sendHtml(subject, body, icsBytes, icsFileName);
        log.info("Contract expiry digest sent: {} expiring contracts (with .ics calendar attachment)",
                expiring.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTML email builder
    // ─────────────────────────────────────────────────────────────────────────

    private String buildHtmlBody(List<ContractSummaryDTO> expiring) {
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
                .badge-warn  { background:#fef9c3; color:#854d0e; padding:2px 8px; border-radius:4px; font-size:12px; }
                .badge-stale { background:#fee2e2; color:#991b1b; padding:2px 8px; border-radius:4px; font-size:12px; }
                .empty { color:#9ca3af; font-style:italic; }
                .footer { margin-top:32px; font-size:11px; color:#9ca3af; }
              </style>
            </head>
            <body>
            """);

        sb.append("<h1>Contracte care expiră curând – CLM Platform</h1>");
        sb.append("<p>Generat automat pe <strong>")
          .append(LocalDate.now().format(DATE_FMT))
          .append("</strong></p>");

        // ── Section 1: expiring contracts ───────────────────────────────────
        sb.append("<h2>&#x26A0; Contracte care expiră în urmatoarele ")
          .append(expiryWarningDays)
          .append(" de zile</h2>");

        if (expiring.isEmpty()) {
            sb.append("<p class=\"empty\">Nu există contracte active care expiră în această perioadă.</p>");
        } else {
            sb.append("""
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Client</th>
                      <th>Data început</th>
                      <th>Data expirare</th>
                      <th>Valoare contract</th>
                      <th>Generat de</th>
                    </tr>
                  </thead>
                  <tbody>
                """);
            for (ContractSummaryDTO c : expiring) {
                sb.append("<tr>")
                  .append("<td>").append(c.getId()).append("</td>")
                  .append("<td>").append(escapeHtml(c.clientLabel())).append("</td>")
                  .append("<td>").append(formatDate(c.getContractStartDate())).append("</td>")
                  .append("<td><span class=\"badge-warn\">").append(formatDate(c.getContractEndDate())).append("</span></td>")
                  .append("<td>").append(formatValue(c)).append("</td>")
                  .append("<td>").append(escapeHtml(orDash(c.getGeneratedByMail()))).append("</td>")
                  .append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        sb.append("""
            <div class="footer">
              Acest email a fost generat automat de CLM Platform – Notifications Service.<br/>
              Nu răspundeți la acest email.
            </div>
            </body></html>
            """);

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "—";
    }

    private String formatValue(ContractSummaryDTO c) {
        return c.getContractValue() != null
                ? String.format("%,.2f RON", c.getContractValue())
                : "—";
    }

    private String orDash(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }

    private String escapeHtml(String text) {
        if (text == null) return "—";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
