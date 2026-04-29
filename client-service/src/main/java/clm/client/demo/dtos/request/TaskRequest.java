package clm.client.demo.dtos.request;

import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequest(
    Boolean done,
    @NotBlank(groups = ValidationGroups.Create.class)
    String title,
    String notes,
    String blocked,
    String objective,
    @NotNull(groups = ValidationGroups.Create.class)
    LocalDateTime date,
    @NotNull(groups = ValidationGroups.Create.class)
    Long userId,
    @NotNull(groups = ValidationGroups.Create.class)
    Long clientId
) {}
