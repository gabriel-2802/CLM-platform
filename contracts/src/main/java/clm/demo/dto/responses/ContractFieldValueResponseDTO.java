package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for contract field value audit records.
 * Contains field values inserted into a generated contract with audit trail.
 * 
 * <p><b>Purpose:</b> Provides a detailed audit trail for each field that was filled
 * in the contract, including the template field reference and timestamp.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractFieldValueResponseDTO {

    private Long id;
    private Long templateFieldId;
    private String fieldLabel;
    private String fieldValue;
}

