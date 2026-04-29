package clm.client.demo.dtos.response;

import clm.client.demo.models.Client;
import clm.client.demo.models.enums.Administration;
import clm.client.demo.models.enums.CompanyType;
import clm.client.demo.models.enums.TaxFrequency;
import clm.client.demo.models.enums.TaxType;

import java.time.LocalDateTime;

public record ClientResponse(
    Long id,
    String denumire,
    CompanyType tip,
    String cui,
    Boolean activa,
    LocalDateTime dataVerificarii,
    String adresa,
    Administration administratie,
    TaxType impozit,
    TaxFrequency platitorTVA,
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
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getType(),
                client.getTaxId(),
                client.isActive(),
                client.getVerificationDate(),
                client.getAddress(),
                client.getAdministration(),
                client.getTaxType(),
                client.getVatPayer(),
                client.getVatOnCollection(),
                client.getHasEuVatCode(),
                client.getEuVatCode(),
                client.getEuOperation(),
                client.getDividends(),
                client.getEmployees(),
                client.getCashRegister(),
                client.getHqExpirationDate(),
                client.getAdminMandateExpiration(),
                client.getFiscalCertificateDate(),
                client.getPayerSheetDate(),
                client.getFiscalVectorDate(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
