package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.YesNoNa;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HistoryRequest(
    @NotNull(groups = ValidationGroups.Create.class)
    Integer anul,
    @NotNull(groups = ValidationGroups.Create.class)
    BigDecimal cifraAfaceri,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean inventar,
    @NotNull(groups = ValidationGroups.Create.class)
    YesNoNa bilantSemIun,
    @NotNull(groups = ValidationGroups.Create.class)
    YesNoNa bilantAnual
) {
}