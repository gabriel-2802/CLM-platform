package clm.demo.mappers;

import clm.demo.dto.responses.ContractFieldValueResponseDTO;
import clm.demo.models.ContractFieldValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ContractFieldValue entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface ContractFieldValueMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "templateField.id", target = "templateFieldId")
    @Mapping(source = "fieldValue", target = "fieldValue")
    @Mapping(source = "createdAt", target = "createdAt")
    ContractFieldValueResponseDTO toResponseDTO(ContractFieldValue entity);
}

