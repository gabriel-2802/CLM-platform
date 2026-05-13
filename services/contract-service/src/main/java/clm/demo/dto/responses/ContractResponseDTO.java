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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ContractResponseDTO {

    private Long id;
    private Long templateId;
    private Integer clientId;
    private String contractStatus;
    private BigDecimal contractValue;
    private BigDecimal contractBalance;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String notes;
    private LocalDate terminationDate;
    private String reasonsForTermination;
    private Boolean autoRenew;

    // Audit Fields
    private LocalDateTime generatedAt;
    private Integer generatedByUser;
    private LocalDateTime uploadedSignedAt;
    private Integer uploadedSignedByUser;
    private LocalDateTime terminatedAt;
    private Integer terminatedByUserId;
    private LocalDateTime modifiedAt;
    private Integer modifiedByUserId;

    private List<DocumentFieldValueResponseDTO> fieldValues;
    private List<AppendixResponseDTO> appendices;
}


