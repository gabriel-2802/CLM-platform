package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppendixResponseDTO {

    private Long id;
    private Long contractId;
    private Long templateId;
    private String title;
    private String appendixStatus;
    private String documentFormat;
    private LocalDateTime generatedAt;
    private Integer generatedByUser;
    private LocalDateTime uploadedSignedAt;
    private Integer uploadedSignedByUser;
    private String notes;

    private List<DocumentFieldValueResponseDTO> fieldValues;
}
