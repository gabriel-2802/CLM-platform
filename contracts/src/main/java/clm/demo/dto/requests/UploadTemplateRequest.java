package clm.demo.dto.requests;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for uploading a contract template file.
 * Contains the file and metadata required to initialize a contract template.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadTemplateRequest {

    @NotNull(message = "File cannot be null")
    private MultipartFile file;

    @NotBlank(message = "Template name cannot be blank")
    private String templateName;

    private String description;
}

