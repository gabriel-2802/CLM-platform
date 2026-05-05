package clm.demo.services.utility;

import clm.demo.exceptions.exceptions.MissingMandatoryFieldException;
import clm.demo.models.Document;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility component for shared document generation logic used by multiple services.
 * Provides methods for validating fields, building field values, and constructing label-to-value mappings.
 */
@Slf4j
@Component
public class DocumentGenerationUtil {

    /**
     * Validates that all required mandatory fields have values in the provided mappings.
     *
     * @param template the document template
     * @param mappings the field label to value mappings
     * @throws MissingMandatoryFieldException if any required fields are missing values
     */
    public void validateMandatoryFields(DocumentTemplate template, Map<String, String> mappings) {
        List<String> missing = template.getTemplateFields().stream()
                .filter(TemplateField::getIsRequired)
                .filter(f -> Objects.nonNull(f.getFieldLabel()))
                .filter(f -> !mappings.containsKey(f.getFieldLabel()) || mappings.get(f.getFieldLabel()).isBlank())
                .map(TemplateField::getFieldLabel)
                .toList();

        if (!missing.isEmpty()) {
            String message = "Missing mandatory field mappings: " + String.join(", ", missing);
            log.warn(message);
            throw new MissingMandatoryFieldException(message, missing);
        }
    }

    /**
     * Builds a list of DocumentFieldValue objects from template fields and provided mappings.
     *
     * @param document the document (Contract or Appendix) to associate with field values
     * @param template the document template
     * @param mappings the field label to value mappings
     * @return a list of DocumentFieldValue objects
     */
    public List<DocumentFieldValue> buildFieldValues(Document document, DocumentTemplate template,
                                                     Map<String, String> mappings) {
        List<DocumentFieldValue> fieldValues = new ArrayList<>();
        for (TemplateField field : template.getTemplateFields()) {
            if (Objects.isNull(field.getFieldLabel())) {
                continue;
            }
            String value = mappings.get(field.getFieldLabel());
            if (Objects.isNull(value) || value.isBlank()) {
                continue;
            }
            fieldValues.add(DocumentFieldValue.builder()
                    .document(document)
                    .templateField(field)
                    .fieldValue(value)
                    .build());
        }
        return fieldValues;
    }

    /**
     * Builds a map from template field labels to their corresponding values.
     * Used for document filling operations.
     *
     * @param fieldValues the list of document field values
     * @return a map of field label to field value
     */
    public Map<String, String> buildLabelValueMap(List<DocumentFieldValue> fieldValues) {
        Map<String, String> map = new HashMap<>(fieldValues.size() * 2);
        for (DocumentFieldValue dfv : fieldValues) {
            TemplateField field = dfv.getTemplateField();
            if (Objects.nonNull(field)
                    && Objects.nonNull(field.getFieldLabel())
                    && Objects.nonNull(dfv.getFieldValue())) {
                map.put(field.getFieldLabel(), dfv.getFieldValue());
            }
        }
        return map;
    }
}