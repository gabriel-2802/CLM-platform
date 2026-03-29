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
 * Allows configuring multiple template fields in a single request.
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldMappingDefinition {

        @NotNull(message = "Field ID cannot be null")
        private Long fieldId;

        @NotNull(message = "Field label cannot be null")
        private String fieldLabel;

        @Builder.Default
        private String dataType = "STRING";

        @Builder.Default
        private boolean isRequired = true;

        @Builder.Default
        private String formatPattern = "";
    }
}