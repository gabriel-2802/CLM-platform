package clm.negotiation.dto.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldValueInfo {
    private String fieldLabel;
    private String fieldValue;
}
