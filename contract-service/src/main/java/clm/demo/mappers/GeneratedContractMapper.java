package clm.demo.mappers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DocumentFieldValueMapper.class, AppendixMapper.class})
public interface GeneratedContractMapper {

    @Mapping(source = "documentTemplate.id", target = "templateId")
    @Mapping(source = "contractStatus",      target = "contractStatus", qualifiedByName = "enumToString")
    ContractResponseDTO toResponseDTO(Contract entity);
}