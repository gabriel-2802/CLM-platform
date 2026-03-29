package clm.demo.mappers;

import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.models.TemplateField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for TemplateField entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface TemplateFieldMapper {

    @Mapping(source = "dataType", target = "dataType", qualifiedByName = "dataTypeToString")
    TemplateResponseDTO.TemplateFieldDTO toFieldDTO(TemplateField entity);

    @Named("dataTypeToString")
    default String dataTypeToString(Object dataType) {
        return dataType != null ? dataType.toString() : null;
    }
}

