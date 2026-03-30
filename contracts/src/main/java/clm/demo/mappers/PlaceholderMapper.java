package clm.demo.mappers;

import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.services.file.actions.FileParserService;
import clm.demo.utils.PlaceHolderUtils;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for PlaceholderInfo entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface PlaceholderMapper {

    ParsedTemplateResponseDTO.PlaceholderDTO toPlaceholderDTO(PlaceHolderUtils.PlaceholderInfo entity);

    PlaceHolderUtils.PlaceholderInfo toEntity(ParsedTemplateResponseDTO.PlaceholderDTO dto);
}

