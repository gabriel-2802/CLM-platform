package clm.demo.dto.requests;

import clm.demo.models.enums.ContractStatus;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public record SearchRequest(String notes, ContractStatus contractStatus, Integer clientId, Integer generatedBy,
                            List<String> labelValues, String templateName, String templateDescription,
                            LocalDate createdAfter, LocalDate createdBefore) {
}
