package clm.demo.mappers;

import clm.demo.dto.responses.FieldMappingResponseDTO;
import clm.demo.models.FieldMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for FieldMapping entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface FieldMappingMapper {

    @Mapping(source = "id", target = "mappingId")
    @Mapping(source = "contractTemplate.id", target = "templateId")
    @Mapping(source = "templateField.id", target = "templateFieldId")
    @Mapping(source = "templateField.fieldName", target = "fieldName")
    @Mapping(source = "dataTransformation", target = "dataTransformation", qualifiedByName = "enumToString")
    @Mapping(source = "mappingStatus", target = "mappingStatus", qualifiedByName = "enumToString")
    FieldMappingResponseDTO toResponseDTO(FieldMapping entity);

    FieldMapping toEntity(FieldMappingResponseDTO dto);

    default String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.toString() : null;
    }
}

