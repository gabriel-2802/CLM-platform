package clm.demo.dto.requests;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateContractRequest {
    @NotNull(message = "Template ID cannot be null")
    private Long templateId;

    @NotNull(message = "Start date cannot be null")
    private LocalDateTime startDate;

    public static class ClientData {
        
    }
}
