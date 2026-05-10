package clm.negotiation.mappers;

import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.models.Negotiation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NegotiationMapper {
    NegotiationResponseDTO toDto(Negotiation negotiation);
}
