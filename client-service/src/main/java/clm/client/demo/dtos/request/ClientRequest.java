package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.Administration;
import clm.client.demo.models.enums.CompanyType;
import clm.client.demo.models.enums.TaxFrequency;
import clm.client.demo.models.enums.TaxType;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClientRequest(
        @NotBlank(groups = ValidationGroups.Create.class)
        @Size(max = 255)
        String name,
        @NotNull(groups = ValidationGroups.Create.class)
        CompanyType type,
        @NotBlank(groups = ValidationGroups.Create.class)
        @Size(max = 64)
        String taxId,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean active,
        LocalDate verificationDate,
        @Size(max = 255)
        String address,
        @NotNull(groups = ValidationGroups.Create.class)
        Administration administration,
        TaxType taxType,
        @NotNull(groups = ValidationGroups.Create.class)
        TaxFrequency vatPayer,
        Boolean vatOnCollection,
        Boolean hasEuVatCode,
        @Size(max = 64)
        String euVatCode,
        Boolean euOperation,
        Boolean dividends,
        @Size(max = 32)
        String employees,
        Boolean cashRegister,
        LocalDate hqExpirationDate,
        LocalDate adminMandateExpiration,
        LocalDate fiscalCertificateDate,
        LocalDate payerSheetDate,
        LocalDate fiscalVectorDate
) {}