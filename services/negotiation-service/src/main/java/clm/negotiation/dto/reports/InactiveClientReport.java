package clm.negotiation.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InactiveClientReport {
    private Integer clientId;
    private String clientName;
    private LocalDateTime lastNegotiationAt;
    private String contractStatus;
    private LocalDate contractEndDate;
}
