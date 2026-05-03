package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.Administration;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record WorkPointRequest(
        @NotBlank(groups = ValidationGroups.Create.class)
        @Size(max = 255)
        String name,
        @NotNull(groups = ValidationGroups.Create.class)
        LocalDate validFrom,
        LocalDate validTo,
        @NotNull(groups = ValidationGroups.Create.class)
        Administration administration,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean ucRegistry,
        @NotNull(groups = ValidationGroups.Create.class)
        Integer employeeCount,
        @Size(max = 64)
        String taxId,
        @NotNull(groups = ValidationGroups.Create.class)
        Boolean cashRegister
) {}