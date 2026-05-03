package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.YesNoNa;

public record DetailsResponse(
        Long id,
        Long clientId,
        Boolean ucRegistry,
        YesNoNa fiscalEvidenceRegistry,
        Boolean moneyLaunderingOffice,
        Boolean internalRules,
        Boolean accountingPoliciesManual,
        Boolean revisalAddress,
        String itmPassword,
        Boolean onlineDeclarations,
        YesNoNa fiscalFileAccess
) {}