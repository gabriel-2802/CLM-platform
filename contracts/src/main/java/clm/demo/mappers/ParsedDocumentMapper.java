package clm.demo.mappers;

import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.services.FileParserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ParsedDocumentResponse to ParsedTemplateResponseDTO conversions.
 */
@Mapper(componentModel = "spring", uses = PlaceholderMapper.class)
public interface ParsedDocumentMapper {

    @Mapping(source = "placeholders", target = "placeholders")
    ParsedTemplateResponseDTO toResponseDTO(FileParserService.ParsedDocumentResponse entity);

    FileParserService.ParsedDocumentResponse toEntity(ParsedTemplateResponseDTO dto);
}

