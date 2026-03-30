package clm.demo.mappers;

import clm.demo.dto.requests.GenContractRequest;
import clm.demo.models.Contract;
import clm.demo.models.Template;
import clm.demo.models.enums.ContractStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MapStruct mapper for converting GenContractRequest to Contract entity.
 * Handles type conversions and default status assignment.
 */
@Mapper(componentModel = "spring")
public interface ContractGenerationMapper {

    /**
     * Maps a GenContractRequest to a Contract entity.
     * Converts Double value to BigDecimal for precise monetary values.
     * Note: contractStatus is set in the service layer.
     *
     * @param request  the incoming contract generation request
     * @param template the contract template associated with this contract
     * @return a Contract entity ready for persistence
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "request.clientId", target = "clientId")
    @Mapping(source = "request.userMail", target = "generatedByMail")
    @Mapping(source = "request.startDate", target = "contractStartDate")
    @Mapping(source = "request.endDate", target = "contractEndDate")
    @Mapping(source = "request.value", target = "contractValue", qualifiedByName = "convertDoubleToBigDecimal")
    @Mapping(target = "contractTemplate", source = "template")
    @Mapping(target = "contractStatus", ignore = true)
    @Mapping(source = "request.notes", target = "notes")
    @Mapping(target = "documentContent", ignore = true)
    @Mapping(target = "fieldValues", ignore = true)
    @Mapping(target = "signedDocument", ignore = true)
    @Mapping(target = "terminationDate", ignore = true)
    @Mapping(target = "reasonsForTermination", ignore = true)
    Contract toContractEntity(GenContractRequest request, Template template);


    /**
     * Converts Double to BigDecimal for precise monetary values.
     */
    @Named("convertDoubleToBigDecimal")
    default BigDecimal convertDoubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
