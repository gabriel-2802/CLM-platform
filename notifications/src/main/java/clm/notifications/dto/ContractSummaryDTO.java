package clm.notifications.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Mirrors ContractResponseDTO from the contracts microservice.
 * Only the fields needed by the notification digest are mapped;
 * unknown fields from the API response are silently ignored.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractSummaryDTO {

    private Long id;
    private Integer clientId;
    private String contractStatus;
    private String generatedByMail;
    private BigDecimal contractValue;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private LocalDate createdAt;
    private List<ContractFieldValueDTO> fieldValues;

    /**
     * Extracts the client name from the injected field values (label "CLIENT_NAME").
     * Falls back to "Client #<clientId>" when not available.
     */
    public String resolveClientName() {
        if (fieldValues != null) {
            return fieldValues.stream()
                    .filter(fv -> "CLIENT_NAME".equalsIgnoreCase(fv.getFieldLabel()))
                    .map(ContractFieldValueDTO::getFieldValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /** Display-friendly client label: name if known, otherwise "Client #N". */
    public String clientLabel() {
        String name = resolveClientName();
        return (name != null) ? name : "Client #" + clientId;
    }
}
