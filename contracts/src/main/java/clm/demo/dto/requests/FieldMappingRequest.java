package clm.demo.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch updating field mappings.
 * Allows mapping multiple template fields to database columns in a single request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldMappingRequest {

    @NotNull(message = "Template ID cannot be null")
    private Long templateId;

    @NotEmpty(message = "Field mappings list cannot be empty")
    @Valid
    private List<FieldMappingDefinition> mappings;

    /**
     * Individual field mapping definition within the batch.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldMappingDefinition {

        @NotNull(message = "Field ID cannot be null")
        private Long fieldId;

        @NotNull(message = "Source table cannot be null")
        private String sourceTable;

        @NotNull(message = "Source column cannot be null")
        private String sourceColumn;

        private String dataTransformation;

        private String description;
    }
}

