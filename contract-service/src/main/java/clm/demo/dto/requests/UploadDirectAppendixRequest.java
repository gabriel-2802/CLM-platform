package clm.demo.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDirectAppendixRequest {

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "File is required")
    private MultipartFile file;

    private Integer userId;
    private String userMail;
    private String notes;
}
