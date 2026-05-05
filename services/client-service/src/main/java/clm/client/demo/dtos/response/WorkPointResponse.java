package clm.client.demo.dtos.response;

import clm.client.demo.models.enums.Administration;

import java.time.LocalDate;

public record WorkPointResponse(
        Long id,
        Long clientId,
        String name,
        LocalDate validFrom,
        LocalDate validTo,
        Administration administration,
        Boolean ucRegistry,
        Integer employeeCount,
        String taxId,
        Boolean cashRegister
) {}