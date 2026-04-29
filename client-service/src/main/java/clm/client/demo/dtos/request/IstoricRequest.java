package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.DaNuNuECazul;

import java.math.BigDecimal;

public record IstoricRequest(
    Integer anul,
    BigDecimal cifraAfaceri,
    Boolean inventar,
    DaNuNuECazul bilantSemIun,
    String bilantAnual
) {
}

