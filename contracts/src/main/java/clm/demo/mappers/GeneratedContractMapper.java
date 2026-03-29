package clm.demo.mappers;

import clm.demo.dto.responses.GeneratedContractResponseDTO;
import clm.demo.models.GeneratedContract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for GeneratedContract entity to DTO conversions.
 */
@Mapper(componentModel = "spring", uses = ContractFieldValueMapper.class)
public interface GeneratedContractMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "contractTemplate.id", target = "templateId")
    @Mapping(source = "contractStatus", target = "contractStatus", qualifiedByName = "contractStatusToString")
    @Mapping(source = "fieldValues", target = "fieldValues")
    GeneratedContractResponseDTO toResponseDTO(GeneratedContract entity);

    @Named("contractStatusToString")
    default String contractStatusToString(Object contractStatus) {
        return contractStatus != null ? contractStatus.toString() : null;
    }
}

