package clm.demo.dto.responses;

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

    private Long templateId;

    private String templateName;

    private String description;

    private Integer fieldCount;

    private Boolean fullyMapped;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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
        private String fieldLabel;
        private String dataType;
        private Integer fieldPosition;
        private Boolean isRequired;
        private String formatPattern;
    }
}
