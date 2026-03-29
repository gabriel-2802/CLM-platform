package clm.demo.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a template field's label.
 * Called by Client Management Service to set display labels for fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFieldLabelRequest {

    @NotNull(message = "Template ID cannot be null")
    private Long templateId;

    @NotNull(message = "Field name cannot be null")
    @NotBlank(message = "Field label cannot be blank")
    private String fieldLabel;
}

