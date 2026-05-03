package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.YesNoNa;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;

public record DetailsRequest(
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean ucRegistry,
        @NotNull(groups = ValidationGroups.Create.class)
        YesNoNa fiscalEvidenceRegistry,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean moneyLaunderingOffice,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean internalRules,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean accountingPoliciesManual,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean revisalAddress,
        String itmPassword,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean onlineDeclarations,
        @NotNull(groups = ValidationGroups.Create.class)
        YesNoNa fiscalFileAccess
) {}