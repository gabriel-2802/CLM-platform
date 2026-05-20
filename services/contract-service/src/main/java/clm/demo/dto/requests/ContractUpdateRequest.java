package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractUpdateRequest(
        @NotNull(message = "User ID is required") Integer userId,
        @NotNull(message = "Appendix ID is required") Integer appendixId,
        @NotNull(message = "Start date is required") LocalDate startDate,
        LocalDate contractEndDate,
        BigDecimal balance,
        BigDecimal value
) {}
