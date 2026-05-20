package clm.demo.mappers;

import clm.demo.dto.responses.ContractDetailsResponseDTO;
import clm.demo.dto.responses.DetailedContractResponseDTO;
import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AppendixMapper.class})
public interface ContractDetailsMapper {

    @Mapping(source = "appendix.id", target = "appendixId")
    ContractDetailsResponseDTO toResponseDTO(ContractDetails entity);

    @Mapping(source = "documentTemplate.id",  target = "templateId")
    @Mapping(source = "contractStatus",        target = "contractStatus", qualifiedByName = "enumToString")
    @Mapping(source = "generatedAt",           target = "generatedAt")
    @Mapping(source = "generatedByUser",       target = "generatedByUser")
    @Mapping(source = "uploadedSignedAt",      target = "uploadedSignedAt")
    @Mapping(source = "uploadedSignedByUser",  target = "uploadedSignedByUser")
    @Mapping(source = "terminatedAt",          target = "terminatedAt")
    @Mapping(source = "terminatedByUserId",    target = "terminatedByUserId")
    @Mapping(source = "contractDetailsList",   target = "contractDetails")
    DetailedContractResponseDTO toDetailedResponseDTO(Contract entity);
}
