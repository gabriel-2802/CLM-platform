package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.Administration;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record WorkPointRequest(
    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(max = 255)
    String denumire,
    @NotNull(groups = ValidationGroups.Create.class)
    LocalDateTime deLa,
    LocalDateTime panaLa,
    @NotNull(groups = ValidationGroups.Create.class)
    Administration administratie,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean registruUC,
    @NotNull(groups = ValidationGroups.Create.class)
    Integer salariati,
    @Size(max = 64)
    String cui,
    @NotNull(groups = ValidationGroups.Create.class)
    Boolean casaDeMarcat
) {
}