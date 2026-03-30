package clm.demo.mappers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for GeneratedContract entity to DTO conversions.
 * Handles conversion from Contract entity to ContractResponseDTO for API responses.
 */
@Mapper(componentModel = "spring", uses = ContractFieldValueMapper.class)
public interface GeneratedContractMapper {

    /**
     * Maps a Contract entity to ContractResponseDTO.
     * Converts enum status to string and handles all contract details.
     *
     * @param entity the Contract entity
     * @return the ContractResponseDTO ready for API response
     */
    @Mapping(source = "contractTemplate.id", target = "templateId")
    @Mapping(source = "contractStatus", target = "contractStatus", qualifiedByName = "contractStatusToString")
    @Mapping(source = "fieldValues", target = "fieldValues")
    ContractResponseDTO toResponseDTO(Contract entity);

    /**
     * Converts the ContractStatus enum to its string representation.
     * 
     * @param contractStatus the enum value
     * @return the string representation (e.g., "PENDING_SIGNATURE")
     */
    @Named("contractStatusToString")
    default String contractStatusToString(Object contractStatus) {
        return contractStatus != null ? contractStatus.toString() : null;
    }
}

