package clm.demo.mappers;

import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.models.Appendix;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Objects;

@Mapper(componentModel = "spring", uses = DocumentFieldValueMapper.class)
public interface AppendixMapper {

    @Mapping(source = "contract.id",          target = "contractId")
    @Mapping(source = "documentTemplate.id",  target = "templateId")
    @Mapping(source = "appendixStatus",       target = "appendixStatus",  qualifiedByName = "enumToString")
    @Mapping(source = "documentFormat",       target = "documentFormat",  qualifiedByName = "enumToString")
    @Mapping(source = "generatedAt",          target = "generatedAt")
    @Mapping(source = "generatedByUser",      target = "generatedByUser")
    @Mapping(source = "uploadedSignedAt",     target = "uploadedSignedAt")
    @Mapping(source = "uploadedSignedByUser", target = "uploadedSignedByUser")
    AppendixResponseDTO toResponseDTO(Appendix entity);

    @Named("enumToString")
    default String enumToString(Object value) {
        return Objects.nonNull(value) ? value.toString() : null;
    }
}