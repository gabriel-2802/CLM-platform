package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.YesNoNa;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;

public record DetailsRequest(
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean registruUC,
    @NotNull(groups = ValidationGroups.Create.class)
    YesNoNa registruEvFiscala,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean ofSpalareBani,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean regulamentOrdineInterioara,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean manualPoliticiContabile,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean adresaRevisal,
    String parolaITM,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean depunereDeclaratiiOnline,
    @NotNull(groups = ValidationGroups.Create.class)
    YesNoNa accesDosarFiscal
) {
}