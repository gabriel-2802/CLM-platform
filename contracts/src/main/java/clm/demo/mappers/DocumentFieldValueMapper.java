package clm.demo.mappers;

import clm.demo.dto.responses.DocumentFieldValueResponseDTO;
import clm.demo.models.DocumentFieldValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentFieldValueMapper {

    @Mapping(source = "templateField.id",         target = "templateFieldId")
    @Mapping(source = "templateField.fieldLabel",  target = "fieldLabel")
    DocumentFieldValueResponseDTO toResponseDTO(DocumentFieldValue entity);
}
