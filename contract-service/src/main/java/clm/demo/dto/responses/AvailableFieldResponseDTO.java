package clm.demo.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for available mapping fields.
 * Provides the list of fields that can be automatically populated from the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableFieldResponseDTO {
    private List<FieldGroup> groups;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldGroup {
        private String groupName;
        private List<FieldOption> options;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldOption {
        private String label;
        private String value;
        private String description;
    }
}
