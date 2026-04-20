package clm.demo.mappers;

import clm.demo.dto.requests.GenContractRequest;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ContractGenerationMapper {

    @Mapping(target = "id",                    ignore = true)
    @Mapping(source = "request.clientId",      target = "clientId")
    @Mapping(source = "request.userMail",      target = "generatedByMail")
    @Mapping(source = "request.startDate",     target = "contractStartDate")
    @Mapping(source = "request.endDate",       target = "contractEndDate")
    @Mapping(source = "request.value",         target = "contractValue", qualifiedByName = "toDecimal")
    @Mapping(source = "template",              target = "documentTemplate")
    @Mapping(source = "request.notes",         target = "notes")
    @Mapping(target = "contractStatus",        ignore = true)
    @Mapping(target = "documentContent",       ignore = true)
    @Mapping(target = "documentFormat",        ignore = true)
    @Mapping(target = "fieldValues",           ignore = true)
    @Mapping(target = "signedDocumentContent", ignore = true)
    @Mapping(target = "terminationDate",       ignore = true)
    @Mapping(target = "reasonsForTermination", ignore = true)
    @Mapping(target = "appendices",            ignore = true)
    Contract toContractEntity(GenContractRequest request, DocumentTemplate template);

    @Named("toDecimal")
    default BigDecimal toDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
