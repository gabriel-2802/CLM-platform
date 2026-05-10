package clm.demo.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RenegotiateContractRequest {
    private BigDecimal contractValue;
    private LocalDate  contractEndDate;
}
