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

    @Mapping(source = "documentTemplate.id",  target = "templateId")
    @Mapping(source = "contractStatus",       target = "contractStatus", qualifiedByName = "enumToString")
    @Mapping(source = "generatedAt",          target = "generatedAt")
    @Mapping(source = "generatedByUser",      target = "generatedByUser")
    @Mapping(source = "uploadedSignedAt",     target = "uploadedSignedAt")
    @Mapping(source = "uploadedSignedByUser", target = "uploadedSignedByUser")
    @Mapping(source = "terminatedAt",         target = "terminatedAt")
    @Mapping(source = "terminatedByUserId",   target = "terminatedByUserId")
    @Mapping(target = "contractValue",        ignore = true)
    @Mapping(target = "contractBalance",      ignore = true)
    @Mapping(target = "contractStartDate",    ignore = true)
    @Mapping(target = "contractEndDate",      ignore = true)
    @Mapping(target = "modifiedAt",           ignore = true)
    @Mapping(target = "modifiedByUserId",     ignore = true)
    ContractResponseDTO toResponseDTO(Contract entity);

    @AfterMapping
    default void enrichFromContractDetails(Contract entity, @MappingTarget ContractResponseDTO dto) {
        List<ContractDetails> details = entity.getContractDetailsList();
        if (details == null || details.isEmpty()) return;

        ContractDetails present = resolvePresentValid(details);
        if (present != null) {
            dto.setContractValue(present.getContractValue());
            dto.setContractBalance(present.getContractBalance());
            dto.setModifiedAt(present.getCreatedAt());
            dto.setModifiedByUserId(present.getCreatedByUserId());
        }

        details.stream()
               .filter(d -> d.getStartDate() != null)
               .min(Comparator.comparing(ContractDetails::getStartDate))
               .ifPresent(d -> dto.setContractStartDate(d.getStartDate()));

        details.stream()
               .filter(d -> d.getEndDate() != null)
               .max(Comparator.comparing(ContractDetails::getCreatedAt))
               .ifPresent(d -> dto.setContractEndDate(d.getEndDate()));
    }

    default ContractDetails resolvePresentValid(List<ContractDetails> details) {
        if (details.size() == 1) return details.get(0);
        LocalDate today = LocalDate.now();
        return details.stream()
                .filter(d -> d.getStartDate() != null && d.getEndDate() != null)
                .filter(d -> !d.getStartDate().isAfter(today) && !d.getEndDate().isBefore(today))
                .max(Comparator.comparing(ContractDetails::getCreatedAt))
                .orElse(null);
    }
}
