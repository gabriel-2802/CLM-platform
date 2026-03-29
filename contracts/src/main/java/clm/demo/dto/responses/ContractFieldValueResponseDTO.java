package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for contract field value audit records.
 * Contains field values inserted into a generated contract with audit trail.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractFieldValueResponseDTO {

    private Long id;

    @JsonProperty("templateFieldId")
    private Long templateFieldId;

    private String fieldValue;

    private LocalDateTime createdAt;
}

