package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.Administration;
import clm.client.demo.models.enums.CompanyType;
import clm.client.demo.models.enums.TaxFrequency;
import clm.client.demo.models.enums.TaxType;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ClientRequest(
    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(max = 255)
    String denumire,
    @NotNull(groups = ValidationGroups.Create.class)
    CompanyType tip,
    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(max = 64)
    String cui,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean activa,
    LocalDateTime dataVerificarii,
    @Size(max = 255)
    String adresa,
    @NotNull(groups = ValidationGroups.Create.class)
    Administration administratie,
    TaxType impozit,
    @NotNull(groups = ValidationGroups.Create.class)
    TaxFrequency platitorTVA,
    Boolean tvaLaIncasare,
    Boolean areCodTVAUE,
    @Size(max = 64)
    String codTVAUE,
    Boolean operatiuneUE,
    Boolean dividende,
    @Size(max = 32)
    String salariati,
    Boolean casaDeMarcat,
    LocalDateTime dataExpSediuSocial,
    LocalDateTime dataExpMandatAdmin,
    LocalDateTime dataCertificatFiscal,
    LocalDateTime dataFisaPlatitor,
    LocalDateTime dataVectFiscal
) {
}

