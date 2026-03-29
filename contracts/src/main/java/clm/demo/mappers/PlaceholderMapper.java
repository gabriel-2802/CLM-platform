package clm.demo.mappers;

import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.services.FileParserService;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for PlaceholderInfo entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface PlaceholderMapper {

    ParsedTemplateResponseDTO.PlaceholderDTO toPlaceholderDTO(FileParserService.PlaceholderInfo entity);

    FileParserService.PlaceholderInfo toEntity(ParsedTemplateResponseDTO.PlaceholderDTO dto);
}

