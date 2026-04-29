package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.Administratie;

import java.time.LocalDateTime;

public record PunctDeLucruRequest(
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

