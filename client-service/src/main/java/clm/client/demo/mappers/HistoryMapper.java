package clm.client.demo.mappers;

import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.models.ClientHistory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "year", ignore = true)
    void updateEntity(@MappingTarget ClientHistory history, HistoryRequest request);

    @Mapping(target = "clientId", source = "client.id")
    HistoryResponse toResponse(ClientHistory history);
}