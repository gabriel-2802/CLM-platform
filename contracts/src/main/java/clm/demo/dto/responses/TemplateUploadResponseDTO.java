package clm.demo.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for template upload operations.
 * Contains the uploaded template details with placeholders replaced by field IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateUploadResponseDTO {
    private Long templateId;

    private String templateName;

    private String documentText;
}