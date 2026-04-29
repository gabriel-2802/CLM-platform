package clm.client.demo.dtos.request;

import clm.client.demo.models.enums.CompanyType;

public record ClientListRequest(
    Boolean active,
    CompanyType tip,
    Long userId,
    Integer page,
    Integer size
) {
}


