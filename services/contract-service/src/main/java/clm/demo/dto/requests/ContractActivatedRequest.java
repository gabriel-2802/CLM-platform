package clm.demo.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractActivatedRequest(

        @NotNull(message = "contractId is required")
        Long contractId,

        @NotNull(message = "clientId is required")
        Integer clientId,

        @NotNull(message = "startDate is required")
        LocalDate startDate,

        LocalDate endDate,

        BigDecimal contractValue
) {}
