package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RenegotiateContractRequest {
    @NotNull(message = "User ID is required")
    private Integer    userId;
    @NotNull(message = "Appendix ID is required")
    private Integer    appendixId;
    private BigDecimal contractValue;
    private LocalDate  contractEndDate;
}
