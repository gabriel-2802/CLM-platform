package clm.demo.mappers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {DocumentFieldValueMapper.class, AppendixMapper.class})
public interface GeneratedContractMapper {

    @Mapping(source = "entity.documentTemplate.id",           target = "templateId")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(source = "entity.contractStatus",        target = "contractStatus", qualifiedByName = "enumToString")
    @Mapping(source = "entity.generatedAt",           target = "generatedAt")
    @Mapping(source = "entity.generatedByUser",       target = "generatedByUser")
    @Mapping(source = "entity.uploadedSignedAt",      target = "uploadedSignedAt")
    @Mapping(source = "entity.uploadedSignedByUser",  target = "uploadedSignedByUser")
    @Mapping(source = "entity.terminatedAt",          target = "terminatedAt")
    @Mapping(source = "entity.terminatedByUserId",    target = "terminatedByUserId")
    @Mapping(source = "contractDetails.contractValue",      target = "contractValue")
    @Mapping(source = "contractDetails.contractBalance",    target = "contractBalance")
    @Mapping(source = "entity.startDate",          target = "contractStartDate")
    @Mapping(source = "contractDetails.endDate",            target = "contractEndDate")
    @Mapping(source = "contractDetails.createdAt",          target = "modifiedAt")
    @Mapping(source = "contractDetails.createdByUserId",    target = "modifiedByUserId")
    ContractResponseDTO toResponseDTO(Contract entity, ContractDetails contractDetails);
}