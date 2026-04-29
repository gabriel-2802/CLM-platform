package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.DaNuNuECazul;

public record DetaliiRequest(
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

