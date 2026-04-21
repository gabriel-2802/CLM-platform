package clm.notifications.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors ContractFieldValueResponseDTO from the contracts microservice.
 * Only the fields needed by the notification digest are mapped.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractFieldValueDTO {
    private String fieldLabel;
    private String fieldValue;
}
