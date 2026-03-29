package clm.demo.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating field mappings.
 * Contains the mapping details to link a template field to a source database column.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFieldMappingRequest {

    @NotNull(message = "Template field ID cannot be null")
    private Long templateFieldId;

    @NotBlank(message = "Source table cannot be blank")
    private String sourceTable;

    @NotBlank(message = "Source column cannot be blank")
    private String sourceColumn;

    private String dataTransformation;

    private String description;
}

