package clm.demo.mappers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {DocumentFieldValueMapper.class, AppendixMapper.class})
public interface GeneratedContractMapper {

    @Mapping(source = "documentTemplate.id", target = "templateId")
    @Mapping(source = "contractStatus",      target = "contractStatus", qualifiedByName = "enumToString")
    @Mapping(source = "fieldValues",         target = "fieldValues")
    @Mapping(source = "appendices",          target = "appendices")
    ContractResponseDTO toResponseDTO(Contract entity);

    @Named("enumToString")
    default String enumToString(Object value) {
        return value != null ? value.toString() : null;
    }
}
