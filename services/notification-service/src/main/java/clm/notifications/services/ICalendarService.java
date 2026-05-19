package clm.notifications.services;

import clm.notifications.dto.ContractSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Builds iCalendar (.ics) files for contract expiry notifications.
 *
 * <p>For each contract two all-day VEVENTs are produced:
 * <ul>
 *   <li>An alert 30 days before expiration ("Contract X expiră în 30 de zile")</li>
 *   <li>An alert on the actual expiration date ("Contract X expiră azi")</li>
 * </ul>
 * The resulting byte array can be attached to an email so recipients can
 * import both events into Google Calendar, Outlook, or Apple Calendar with
 * a single click.</p>
 */
@Slf4j
@Service
public class ICalendarService {

    private static final DateTimeFormatter DATE_FMT   = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter STAMP_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    /**
     * Produces a single .ics file containing two VEVENT blocks per contract.
     *
     * @param contracts expiring contracts to include
     * @return UTF-8 encoded iCalendar bytes, ready to attach to an email
     */
    public byte[] buildCalendarFile(List<ContractSummaryDTO> contracts) {
        String dtstamp = ZonedDateTime.now(ZoneOffset.UTC).format(STAMP_FMT);
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//CLM Platform//Contract Notifications//RO\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:CLM – Expirări contracte\r\n");
        sb.append("X-WR-TIMEZONE:Europe/Bucharest\r\n");

        for (ContractSummaryDTO contract : contracts) {
            if (contract.getContractEndDate() == null) continue;

            String label = contract.clientLabel();

            appendExpiryEvent(sb, contract, label, dtstamp);
            appendWarningEvent(sb, contract, label, dtstamp);
        }

        sb.append("END:VCALENDAR\r\n");

        log.debug("Generated .ics with {} contracts ({} VEVENTs)", contracts.size(), contracts.size() * 2);
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void appendExpiryEvent(StringBuilder sb, ContractSummaryDTO c,
                                   String label, String dtstamp) {
        LocalDate expiry = c.getContractEndDate();
        String uid = "clm-contract-" + c.getId() + "-expiry-" + UUID.randomUUID() + "@clm.platform";

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(dtstamp).append("\r\n");
        sb.append("DTSTART;VALUE=DATE:").append(expiry.format(DATE_FMT)).append("\r\n");
        sb.append("DTEND;VALUE=DATE:").append(expiry.plusDays(1).format(DATE_FMT)).append("\r\n");
        sb.append("SUMMARY:Contract #").append(c.getId()).append(" – ").append(fold(label)).append(" expiră azi\r\n");
        sb.append("DESCRIPTION:Contractul cu ").append(fold(label))
          .append(" (ID: ").append(c.getId()).append(")")
          .append(" a expirat. Valoare: ").append(formatValue(c)).append(".\r\n");
        sb.append("STATUS:CONFIRMED\r\n");
        sb.append("TRANSP:TRANSPARENT\r\n");
        sb.append("BEGIN:VALARM\r\n");
        sb.append("TRIGGER:-PT0M\r\n");
        sb.append("ACTION:DISPLAY\r\n");
        sb.append("DESCRIPTION:Contract expirat\r\n");
        sb.append("END:VALARM\r\n");
        sb.append("END:VEVENT\r\n");
    }

    private void appendWarningEvent(StringBuilder sb, ContractSummaryDTO c,
                                    String label, String dtstamp) {
        LocalDate warning = c.getContractEndDate().minusDays(30);
        String uid = "clm-contract-" + c.getId() + "-warning-" + UUID.randomUUID() + "@clm.platform";

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(dtstamp).append("\r\n");
        sb.append("DTSTART;VALUE=DATE:").append(warning.format(DATE_FMT)).append("\r\n");
        sb.append("DTEND;VALUE=DATE:").append(warning.plusDays(1).format(DATE_FMT)).append("\r\n");
        sb.append("SUMMARY:⚠ Contract #").append(c.getId()).append(" – ").append(fold(label)).append(" expiră în 30 de zile\r\n");
        sb.append("DESCRIPTION:Contractul cu ").append(fold(label))
          .append(" (ID: ").append(c.getId()).append(")")
          .append(" expiră pe ").append(c.getContractEndDate()).append(".")
          .append(" Valoare: ").append(formatValue(c)).append(".\r\n");
        sb.append("STATUS:CONFIRMED\r\n");
        sb.append("TRANSP:TRANSPARENT\r\n");
        sb.append("BEGIN:VALARM\r\n");
        sb.append("TRIGGER:-PT0M\r\n");
        sb.append("ACTION:DISPLAY\r\n");
        sb.append("DESCRIPTION:Contract expiră în 30 de zile\r\n");
        sb.append("END:VALARM\r\n");
        sb.append("END:VEVENT\r\n");
    }

    /** Escapes commas and semicolons per RFC 5545. */
    private String fold(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;");
    }

    private String formatValue(ContractSummaryDTO c) {
        return c.getContractValue() != null
                ? String.format("%,.2f RON", c.getContractValue())
                : "—";
    }
}
