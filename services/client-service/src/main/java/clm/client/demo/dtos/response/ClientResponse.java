package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.Administration;
import clm.client.demo.models.enums.CompanyType;
import clm.client.demo.models.enums.TaxFrequency;
import clm.client.demo.models.enums.TaxType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        CompanyType type,
        String taxId,
        Boolean active,
        LocalDate verificationDate,
        String address,
        Administration administration,
        TaxType taxType,
        TaxFrequency vatPayer,
        Boolean vatOnCollection,
        Boolean hasEuVatCode,
        String euVatCode,
        Boolean euOperation,
        Boolean dividends,
        String employees,
        Boolean cashRegister,
        LocalDate hqExpirationDate,
        LocalDate adminMandateExpiration,
        LocalDate fiscalCertificateDate,
        LocalDate payerSheetDate,
        LocalDate fiscalVectorDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}