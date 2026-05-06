package clm.demo.mappers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DocumentFieldValueMapper.class, AppendixMapper.class})
public interface GeneratedContractMapper {

    @Mapping(source = "documentTemplate.id", target = "templateId")
    @Mapping(source = "contractStatus",      target = "contractStatus", qualifiedByName = "enumToString")
    @Mapping(source = "generatedAt",         target = "generatedAt")
    @Mapping(source = "generatedByUserId",   target = "generatedByUserId")
    @Mapping(source = "terminatedAt",        target = "terminatedAt")
    @Mapping(source = "terminatedByUserId",  target = "terminatedByUserId")
    @Mapping(source = "uploadedSignedAt",    target = "uploadedSignedAt")
    @Mapping(source = "uploadedSignedByUserId", target = "uploadedSignedByUserId")
    ContractResponseDTO toResponseDTO(Contract entity);
}

