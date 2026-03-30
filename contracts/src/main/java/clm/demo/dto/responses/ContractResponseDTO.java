package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractResponseDTO {
    
    private Long id;
    private Long templateId;
    private Integer clientId;
    private String contractStatus;
    private Integer generatedBy;
    private String generatedByMail;
    private BigDecimal contractValue;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String notes;

    private LocalDate terminationDate;
    private String reasonsForTermination;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ContractFieldValueResponseDTO> fieldValues;
}

