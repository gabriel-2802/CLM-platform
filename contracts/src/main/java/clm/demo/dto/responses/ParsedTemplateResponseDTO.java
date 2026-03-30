package clm.demo.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for file parsing operations.
 * Contains parsed document content and extracted placeholders.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedTemplateResponseDTO {
    private Long templateId;

    private String templateName;

    private String documentText;

    private int placeholderCount;

    private List<PlaceholderDTO> placeholders;

    /**
     * Nested DTO for placeholder details.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceholderDTO {
        private int position;
        private String placeholderText;
        private int startIndex;
        private int endIndex;
        private Long fieldId;
    }
}

