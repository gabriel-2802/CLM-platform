package clm.negotiation.dto.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractSummaryInfo {

    private Long id;
    private String contractStatus;
    private LocalDate contractEndDate;
    private BigDecimal contractValue;
    private List<FieldValueInfo> fieldValues;

    public String resolveClientName() {
        if (fieldValues == null) return null;
        return fieldValues.stream()
                .filter(fv -> "CLIENT_NAME".equalsIgnoreCase(fv.getFieldLabel()))
                .map(FieldValueInfo::getFieldValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);
    }

    public String clientLabel(Integer clientId) {
        String name = resolveClientName();
        return name != null ? name : "Client #" + clientId;
    }
}
