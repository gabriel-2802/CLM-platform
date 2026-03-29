package clm.demo.dto.responses;

import clm.demo.models.TemplateField;
import clm.demo.models.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single template field.
 * Contains field metadata and formatting rules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateFieldResponseDTO {

    private Long id;
    private String fieldLabel;
    private DataType dataType;
    private String placeholderText;
    private Integer fieldPosition;
    private Boolean isRequired;
    private String formatPattern;

    /**
     * Constructor to map from TemplateField entity.
     */
    public TemplateFieldResponseDTO(TemplateField field) {
        this.id = field.getId();
        this.fieldLabel = field.getFieldLabel();
        this.dataType = field.getDataType();
        this.placeholderText = field.getPlaceholderText();
        this.fieldPosition = field.getFieldPosition();
        this.isRequired = field.getIsRequired();
        this.formatPattern = field.getFormatPattern();
    }
}

