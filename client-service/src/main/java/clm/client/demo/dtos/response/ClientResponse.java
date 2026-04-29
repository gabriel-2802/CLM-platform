package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.Administratie;
import clm.client.demo.models.enums.DaLunarTrim;
import clm.client.demo.models.enums.Impozit;
import clm.client.demo.models.enums.Tip;

import java.time.LocalDateTime;

public record ClientResponse(
    Long id,
    String denumire,
    Tip tip,
    String cui,
    Boolean activa,
    LocalDateTime dataVerificarii,
    String adresa,
    Administratie administratie,
    Impozit impozit,
    DaLunarTrim platitorTVA,
    Boolean tvaLaIncasare,
    Boolean areCodTVAUE,
    String codTVAUE,
    Boolean operatiuneUE,
    Boolean dividende,
    String salariati,
    Boolean casaDeMarcat,
    LocalDateTime dataExpSediuSocial,
    LocalDateTime dataExpMandatAdmin,
    LocalDateTime dataCertificatFiscal,
    LocalDateTime dataFisaPlatitor,
    LocalDateTime dataVectFiscal,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

