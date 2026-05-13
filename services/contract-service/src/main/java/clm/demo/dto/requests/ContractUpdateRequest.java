package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractUpdateRequest(
        @NotNull(message = "User ID is required") Integer userId,
        LocalDate contractEndDate,
        BigDecimal balance,
        BigDecimal value
) {}
