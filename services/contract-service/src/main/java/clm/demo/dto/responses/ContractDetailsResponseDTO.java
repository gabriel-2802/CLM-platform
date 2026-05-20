package clm.demo.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ContractDetailsResponseDTO {

    private Long          id;
    private BigDecimal    contractValue;
    private BigDecimal    contractBalance;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private LocalDateTime createdAt;
    private Integer       createdByUserId;
    private Long          appendixId;
}
