package clm.client.demo.mappers;

import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.response.ClientResponse;
import clm.client.demo.models.Client;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "workPoints", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "userClients", ignore = true)
    Client toEntity(ClientRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "workPoints", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "userClients", ignore = true)
    void partialUpdateEntity(@MappingTarget Client client, ClientRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "workPoints", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "userClients", ignore = true)
    void updateEntity(@MappingTarget Client client, ClientRequest request);

    ClientResponse toResponse(Client client);
}