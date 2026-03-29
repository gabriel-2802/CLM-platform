package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for generated contract operations.
 * Contains details of a filled and generated contract document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedContractResponseDTO {

    private Long id;

    @JsonProperty("templateId")
    private Long templateId;

    private Integer clientId;

    private String contractStatus;

    private Integer generatedBy;

    private BigDecimal contractValue;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    private String notes;

    private LocalDateTime createdAt;

    private List<ContractFieldValueDTO> fieldValues;

    /**
     * Nested DTO for contract field value details.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractFieldValueDTO {
        private Long id;
        private Long templateFieldId;
        private String fieldValue;
        private LocalDateTime createdAt;
    }
}

