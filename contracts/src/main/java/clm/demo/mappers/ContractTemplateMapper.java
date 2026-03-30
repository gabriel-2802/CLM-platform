package clm.demo.mappers;

import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.models.Template;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ContractTemplate entity to DTO conversions.
 */
@Mapper(componentModel = "spring", uses = TemplateFieldMapper.class)
public interface ContractTemplateMapper {

    @Mapping(source = "id", target = "templateId")
    @Mapping(source = "isFullyMapped", target = "fullyMapped")
    @Mapping(source = "templateFields", target = "fields")
    TemplateResponseDTO toResponseDTO(Template entity);
}

