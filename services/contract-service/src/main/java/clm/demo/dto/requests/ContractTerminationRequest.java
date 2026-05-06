package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContractTerminationRequest {
    @NotNull(message = "Termination date is required!")
    private LocalDate terminationDate;

    private Integer userId;

    private String reasons;
}
