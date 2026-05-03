package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.YesNoNa;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HistoryRequest(
        @NotNull(groups = ValidationGroups.Create.class)
        Integer year,
        @NotNull(groups = ValidationGroups.Create.class)
        BigDecimal turnover,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean inventory,
        @NotNull(groups = ValidationGroups.Create.class)
        YesNoNa juneSemesterBalance,
        @NotNull(groups = ValidationGroups.Create.class)
        YesNoNa annualBalance
) {}