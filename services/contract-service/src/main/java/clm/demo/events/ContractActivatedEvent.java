package clm.demo.events;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractActivatedEvent(
        Long contractId,
        Integer clientId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal contractValue
) {}
