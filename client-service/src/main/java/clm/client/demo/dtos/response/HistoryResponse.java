package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.YesNoNa;

import java.math.BigDecimal;

public record HistoryResponse(
        Long id,
        Long clientId,
        Integer year,
        BigDecimal turnover,
        Boolean inventory,
        YesNoNa juneSemesterBalance,
        YesNoNa annualBalance
) {}