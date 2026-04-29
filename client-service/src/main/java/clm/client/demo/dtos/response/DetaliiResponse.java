package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.DaNuNuECazul;

public record DetaliiResponse(
    Long id,
    Long clientId,
    Boolean registruUC,
    DaNuNuECazul registruEvFiscala,
    Boolean ofSpalareBani,
    Boolean regulamentOrdineInterioara,
    Boolean manualPoliticiContabile,
    Boolean adresaRevisal,
    String parolaITM,
    Boolean depunereDeclaratiiOnline,
    DaNuNuECazul accesDosarFiscal
) {
}

