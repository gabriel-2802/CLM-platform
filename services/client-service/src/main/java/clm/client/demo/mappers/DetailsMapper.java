package clm.client.demo.mappers;

import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.models.ClientDetails;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DetailsMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    void updateEntity(@MappingTarget ClientDetails details, DetailsRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    void partialUpdateEntity(@MappingTarget ClientDetails details, DetailsRequest request);

    DetailsResponse toResponse(ClientDetails details);
}