package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.DaNuNuECazul;

import java.math.BigDecimal;

public record IstoricResponse(
    Long id,
    Long clientId,
    Integer anul,
    BigDecimal cifraAfaceri,
    Boolean inventar,
    DaNuNuECazul bilantSemIun,
    String bilantAnual
) {
}

