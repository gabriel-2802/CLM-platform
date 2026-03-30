package clm.demo.models.converters;

import clm.demo.models.enums.ContractStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts ContractStatus enum to/from PostgreSQL contract_status_enum type.
 * Ensures proper type casting when persisting to the database.
 */
@Converter(autoApply = true)
public class ContractStatusConverter implements AttributeConverter<ContractStatus, String> {

    @Override
    public String convertToDatabaseColumn(ContractStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ContractStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ContractStatus.valueOf(dbData);
    }
}


