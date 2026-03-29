package clm.demo.models.converters;

import clm.demo.models.enums.ContractGenerationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts ContractGenerationStatus enum to/from PostgreSQL contract_status_enum type.
 * Ensures proper type casting when persisting to the database.
 */
@Converter(autoApply = true)
public class ContractGenerationStatusConverter implements AttributeConverter<ContractGenerationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ContractGenerationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ContractGenerationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ContractGenerationStatus.valueOf(dbData);
    }
}

