package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.CompanyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ClientListRequest(
        Boolean active,
        CompanyType type,
        Long userId,
        @Min(0) Integer page,
        @Min(1) @Max(200) Integer size
) {}