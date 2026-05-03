package clm.client.demo.mappers;

import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.models.WorkPoint;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WorkPointMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    WorkPoint toEntity(WorkPointRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    void updateEntity(@MappingTarget WorkPoint workPoint, WorkPointRequest request);

    @Mapping(target = "clientId", source = "client.id")
    WorkPointResponse toResponse(WorkPoint workPoint);
}