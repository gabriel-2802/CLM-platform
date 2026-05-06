package clm.demo.mappers;

import clm.demo.dto.requests.GenContractRequest;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ContractGenerationMapper {

    @Mapping(target = "id",                    ignore = true)
    @Mapping(source = "request.clientId",      target = "clientId")
    @Mapping(source = "request.userId",        target = "generatedByUserId",      qualifiedByName = "toInteger")
    @Mapping(source = "request.startDate",     target = "contractStartDate")
    @Mapping(source = "request.endDate",       target = "contractEndDate")
    @Mapping(source = "request.value",         target = "contractValue")
    @Mapping(source = "request.contractBalance", target = "contractBalance")
    @Mapping(source = "template",              target = "documentTemplate")
    @Mapping(target = "contractStatus",        ignore = true)
    @Mapping(target = "documentContent",       ignore = true)
    @Mapping(target = "documentFormat",        ignore = true)
    @Mapping(target = "fieldValues",           ignore = true)
    @Mapping(target = "signedDocumentContent", ignore = true)
    @Mapping(target = "terminationDate",       ignore = true)
    @Mapping(target = "reasonsForTermination", ignore = true)
    @Mapping(target = "appendices",            ignore = true)
    @Mapping(target = "generatedAt",           ignore = true)
    @Mapping(target = "terminatedAt",          ignore = true)
    @Mapping(target = "terminatedByUserId",    ignore = true)
    @Mapping(target = "uploadedSignedAt",      ignore = true)
    @Mapping(target = "uploadedSignedByUserId", ignore = true)
    Contract toContractEntity(GenContractRequest request, DocumentTemplate template);

    /** Narrows Long userId to Integer generatedBy (contract IDs fit well within Integer range). */
    @Named("toInteger")
    default Integer toInteger(Long value) {
        return Objects.nonNull(value) ? value.intValue() : null;
    }
}