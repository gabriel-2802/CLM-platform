package clm.client.demo.dtos.response;

import clm.client.demo.models.ClientDetails;
import clm.client.demo.models.enums.YesNoNa;

public record DetailsResponse(
    Long id,
    Long clientId,
    Boolean registruUC,
    YesNoNa registruEvFiscala,
    Boolean ofSpalareBani,
    Boolean regulamentOrdineInterioara,
    Boolean manualPoliticiContabile,
    Boolean adresaRevisal,
    String parolaITM,
    Boolean depunereDeclaratiiOnline,
    YesNoNa accesDosarFiscal
) {
    public static DetailsResponse from(ClientDetails details) {
        return new DetailsResponse(
                details.getId(),
                details.getClient().getId(),
                details.isUcRegistry(),
                details.getFiscalEvidenceRegistry(),
                details.isMoneyLaunderingOffice(),
                details.isInternalRules(),
                details.isAccountingPoliciesManual(),
                details.isRevisalAddress(),
                details.getItmPassword(),
                details.isOnlineDeclarations(),
                details.getFiscalFileAccess()
        );
    }
}
