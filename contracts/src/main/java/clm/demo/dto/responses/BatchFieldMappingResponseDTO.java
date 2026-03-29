package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for batch field mapping updates.
 * Contains the results of mapping multiple template fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchFieldMappingResponseDTO {

    @JsonProperty("templateId")
    private Long templateId;

    @JsonProperty("totalMappings")
    private int totalMappings;

    @JsonProperty("successfulMappings")
    private int successfulMappings;

    @JsonProperty("failedMappings")
    private int failedMappings;

    @JsonProperty("mappings")
    private List<MappingResult> mappings;

    /**
     * Individual mapping result within the batch response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MappingResult {

        private Long fieldId;

        private Long mappingId;

        private String fieldName;

        private String sourceTable;

        private String sourceColumn;

        private String status;

        private String errorMessage;
    }
}

