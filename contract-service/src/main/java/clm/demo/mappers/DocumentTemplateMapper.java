package clm.demo.mappers;

import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.models.DocumentTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TemplateFieldMapper.class)
public interface DocumentTemplateMapper {

    @Mapping(source = "id",            target = "templateId")
    @Mapping(source = "isFullyMapped", target = "fullyMapped")
    @Mapping(source = "templateFields", target = "fields")
    TemplateResponseDTO toResponseDTO(DocumentTemplate entity);
}
