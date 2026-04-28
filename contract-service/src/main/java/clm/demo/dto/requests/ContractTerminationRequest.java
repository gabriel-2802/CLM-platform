package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class ContractTerminationRequest {
    @NotNull(message = "Termination date is required!")
    private final LocalDate terminationDate;

    private final String reasons;
}
