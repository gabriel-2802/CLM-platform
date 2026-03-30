package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ContractTerminationRequest {
    @NotNull(message = "Termination date is required!")
    private final LocalDateTime terminationDate;

    private final String reasons;
}
