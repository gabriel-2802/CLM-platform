package clm.demo.mappers;

import clm.demo.dto.responses.ContractFieldValueResponseDTO;
import clm.demo.models.ContractFieldValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ContractFieldValue entity to DTO conversions.
 * Handles conversion from audit trail entity to response DTO.
 */
@Mapper(componentModel = "spring")
public interface ContractFieldValueMapper {

    /**
     * Maps a ContractFieldValue entity to ContractFieldValueResponseDTO.
     * Extracts the field label from the related TemplateField.
     *
     * @param entity the ContractFieldValue entity
     * @return the ContractFieldValueResponseDTO for API response
     */
    @Mapping(source = "templateField.id", target = "templateFieldId")
    @Mapping(source = "templateField.fieldLabel", target = "fieldLabel")
    ContractFieldValueResponseDTO toResponseDTO(ContractFieldValue entity);
}

