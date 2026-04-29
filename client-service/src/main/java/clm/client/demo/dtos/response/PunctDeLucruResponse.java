package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.Administratie;

import java.time.LocalDateTime;

public record PunctDeLucruResponse(
    Long id,
    Long clientId,
    String denumire,
    LocalDateTime deLa,
    LocalDateTime panaLa,
    Administratie administratie,
    Boolean registruUC,
    Integer salariati,
    String cui,
    Boolean casaDeMarcat
) {
}

