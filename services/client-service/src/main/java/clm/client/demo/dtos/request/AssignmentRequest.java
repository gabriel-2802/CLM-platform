package clm.client.demo.dtos.request;

import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignmentRequest(
    @NotNull(groups = ValidationGroups.Create.class)
    List<Long> userIds
) {
}

