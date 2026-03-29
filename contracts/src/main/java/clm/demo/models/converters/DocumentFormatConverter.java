package clm.demo.models.converters;

import clm.demo.models.enums.DocumentFormat;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts DocumentFormat enum to/from PostgreSQL document_format_enum type.
 * Ensures proper type casting when persisting to the database.
 */
@Converter(autoApply = true)
public class DocumentFormatConverter implements AttributeConverter<DocumentFormat, String> {

    @Override
    public String convertToDatabaseColumn(DocumentFormat attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public DocumentFormat convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DocumentFormat.valueOf(dbData);
    }
}

