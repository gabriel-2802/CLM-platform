package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for template operations.
 * Contains the contract template details along with its fields and mappings status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateResponseDTO {

    @JsonProperty("id")
    private Long templateId;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("documentFormat")
    private String documentFormat;

    @JsonProperty("fieldCount")
    private Integer fieldCount;

    @JsonProperty("isFullyMapped")
    private Boolean isFullyMapped;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("fields")
    private List<TemplateFieldDTO> fields;

    /**
     * Nested DTO for template field details.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateFieldDTO {
        private Long id;
        private String fieldName;
        private String fieldLabel;
        private String dataType;
        private Integer fieldPosition;
        private Integer pageNumber;
        private Boolean isRequired;
        private String formatPattern;
    }
}
