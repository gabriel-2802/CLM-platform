package clm.demo.models.converters;

import clm.demo.models.enums.DataType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts DataType enum to/from PostgreSQL data_type_enum type.
 * Ensures proper type casting when persisting to the database.
 */
@Converter(autoApply = true)
public class DataTypeConverter implements AttributeConverter<DataType, String> {

    @Override
    public String convertToDatabaseColumn(DataType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public DataType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DataType.valueOf(dbData);
    }
}

