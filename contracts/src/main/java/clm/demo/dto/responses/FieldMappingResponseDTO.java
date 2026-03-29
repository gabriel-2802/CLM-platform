package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for field mapping operations.
 * Contains the mapping details and status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldMappingResponseDTO {

    @JsonProperty("id")
    private Long mappingId;

    @JsonProperty("templateId")
    private Long templateId;

    @JsonProperty("fieldId")
    private Long templateFieldId;

    @JsonProperty("fieldName")
    private String fieldName;

    @JsonProperty("sourceTable")
    private String sourceTable;

    @JsonProperty("sourceColumn")
    private String sourceColumn;

    @JsonProperty("dataTransformation")
    private String dataTransformation;

    @JsonProperty("mappingStatus")
    private String mappingStatus;

    @JsonProperty("description")
    private String description;
}

