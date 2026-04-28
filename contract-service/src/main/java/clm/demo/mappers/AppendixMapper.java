package clm.demo.mappers;

import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.models.Appendix;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = DocumentFieldValueMapper.class)
public interface AppendixMapper {

    @Mapping(source = "contract.id",          target = "contractId")
    @Mapping(source = "documentTemplate.id",  target = "templateId")
    @Mapping(source = "appendixStatus",        target = "appendixStatus", qualifiedByName = "enumToString")
    @Mapping(source = "documentFormat",        target = "documentFormat", qualifiedByName = "enumToString")
    @Mapping(source = "fieldValues",           target = "fieldValues")
    AppendixResponseDTO toResponseDTO(Appendix entity);

    @Named("enumToString")
    default String enumToString(Object value) {
        return value != null ? value.toString() : null;
    }
}
