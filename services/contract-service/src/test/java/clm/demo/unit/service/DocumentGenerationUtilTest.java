package clm.demo.unit.service;

import clm.demo.exceptions.exceptions.MissingMandatoryFieldException;
import clm.demo.models.Document;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.services.utility.DocumentGenerationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentGenerationUtilTest {

    private DocumentGenerationUtil util;

    @BeforeEach
    void setUp() {
        util = new DocumentGenerationUtil();
    }

    // ── validateMandatoryFields ───────────────────────────────────────────────

    @Nested
    class ValidateMandatoryFields {

        @Test
        void should_pass_when_all_required_fields_are_present() {
            DocumentTemplate template = templateWithFields(
                    requiredField("CompanyName"),
                    optionalField("Notes")
            );
            Map<String, String> mappings = Map.of("CompanyName", "Acme Inc", "Notes", "some note");
            // No exception expected
            util.validateMandatoryFields(template, mappings);
        }

        @Test
        void should_throw_when_required_field_is_missing() {
            DocumentTemplate template = templateWithFields(requiredField("SignerName"));
            Map<String, String> mappings = Map.of();

            assertThatThrownBy(() -> util.validateMandatoryFields(template, mappings))
                    .isInstanceOf(MissingMandatoryFieldException.class)
                    .hasMessageContaining("SignerName");
        }

        @Test
        void should_throw_when_required_field_value_is_blank() {
            DocumentTemplate template = templateWithFields(requiredField("CompanyName"));
            Map<String, String> mappings = Map.of("CompanyName", "   ");

            assertThatThrownBy(() -> util.validateMandatoryFields(template, mappings))
                    .isInstanceOf(MissingMandatoryFieldException.class);
        }

        @Test
        void should_not_throw_when_optional_field_is_missing() {
            DocumentTemplate template = templateWithFields(optionalField("Notes"));
            Map<String, String> mappings = Map.of();
            // No exception expected
            util.validateMandatoryFields(template, mappings);
        }

        @Test
        void should_include_all_missing_fields_in_exception_message() {
            DocumentTemplate template = templateWithFields(
                    requiredField("FieldA"),
                    requiredField("FieldB"),
                    requiredField("FieldC")
            );
            Map<String, String> mappings = Map.of("FieldA", "value");

            assertThatThrownBy(() -> util.validateMandatoryFields(template, mappings))
                    .isInstanceOf(MissingMandatoryFieldException.class)
                    .hasMessageContaining("FieldB")
                    .hasMessageContaining("FieldC");
        }

        @Test
        void should_skip_fields_with_null_label() {
            TemplateField unlabelled = TemplateField.builder()
                    .fieldLabel(null)
                    .isRequired(true)
                    .fieldPosition(0)
                    .build();
            DocumentTemplate template = templateWithFields(unlabelled);
            Map<String, String> mappings = Map.of();
            // No exception — unlabelled required fields cannot be validated by label
            util.validateMandatoryFields(template, mappings);
        }
    }

    // ── buildFieldValues ─────────────────────────────────────────────────────

    @Nested
    class BuildFieldValues {

        @Test
        void should_build_field_value_for_each_mapped_field() {
            DocumentTemplate template = templateWithFields(
                    requiredField("Name"),
                    requiredField("Date")
            );
            Document document = mock(Document.class);
            Map<String, String> mappings = Map.of("Name", "Alice", "Date", "2026-01-01");

            List<DocumentFieldValue> result = util.buildFieldValues(document, template, mappings);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(DocumentFieldValue::getFieldValue)
                    .containsExactlyInAnyOrder("Alice", "2026-01-01");
        }

        @Test
        void should_return_empty_list_when_no_mappings_match() {
            DocumentTemplate template = templateWithFields(optionalField("Notes"));
            Document document = mock(Document.class);
            Map<String, String> mappings = Map.of();

            assertThat(util.buildFieldValues(document, template, mappings)).isEmpty();
        }

        @Test
        void should_skip_fields_with_null_label() {
            TemplateField unlabelled = TemplateField.builder()
                    .fieldLabel(null)
                    .isRequired(false)
                    .fieldPosition(0)
                    .build();
            DocumentTemplate template = templateWithFields(unlabelled);
            Document document = mock(Document.class);
            Map<String, String> mappings = Map.of("SomeLabel", "value");

            assertThat(util.buildFieldValues(document, template, mappings)).isEmpty();
        }

        @Test
        void should_skip_fields_with_blank_value_in_mappings() {
            DocumentTemplate template = templateWithFields(optionalField("Remarks"));
            Document document = mock(Document.class);
            Map<String, String> mappings = Map.of("Remarks", "   ");

            assertThat(util.buildFieldValues(document, template, mappings)).isEmpty();
        }

        @Test
        void should_associate_field_value_with_correct_document_and_field() {
            TemplateField field = requiredField("Project");
            DocumentTemplate template = templateWithFields(field);
            Document document = mock(Document.class);
            Map<String, String> mappings = Map.of("Project", "Artemis");

            List<DocumentFieldValue> result = util.buildFieldValues(document, template, mappings);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDocument()).isSameAs(document);
            assertThat(result.get(0).getTemplateField()).isSameAs(field);
            assertThat(result.get(0).getFieldValue()).isEqualTo("Artemis");
        }
    }

    // ── buildLabelValueMap ────────────────────────────────────────────────────

    @Nested
    class BuildLabelValueMap {

        @Test
        void should_build_map_from_field_values() {
            TemplateField field1 = requiredField("Name");
            TemplateField field2 = requiredField("Date");

            DocumentFieldValue dfv1 = DocumentFieldValue.builder()
                    .templateField(field1).fieldValue("Alice").build();
            DocumentFieldValue dfv2 = DocumentFieldValue.builder()
                    .templateField(field2).fieldValue("2026-01-01").build();

            Map<String, String> map = util.buildLabelValueMap(List.of(dfv1, dfv2));

            assertThat(map).containsEntry("Name", "Alice");
            assertThat(map).containsEntry("Date", "2026-01-01");
        }

        @Test
        void should_return_empty_map_for_empty_input() {
            assertThat(util.buildLabelValueMap(List.of())).isEmpty();
        }

        @Test
        void should_skip_entries_with_null_field_value() {
            TemplateField field = requiredField("Notes");
            DocumentFieldValue dfv = DocumentFieldValue.builder()
                    .templateField(field).fieldValue(null).build();

            assertThat(util.buildLabelValueMap(List.of(dfv))).isEmpty();
        }

        @Test
        void should_skip_entries_with_null_template_field() {
            DocumentFieldValue dfv = DocumentFieldValue.builder()
                    .templateField(null).fieldValue("someValue").build();

            assertThat(util.buildLabelValueMap(List.of(dfv))).isEmpty();
        }

        @Test
        void should_skip_entries_with_null_field_label() {
            TemplateField unlabelled = TemplateField.builder()
                    .fieldLabel(null).isRequired(false).build();
            DocumentFieldValue dfv = DocumentFieldValue.builder()
                    .templateField(unlabelled).fieldValue("someValue").build();

            assertThat(util.buildLabelValueMap(List.of(dfv))).isEmpty();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static DocumentTemplate templateWithFields(TemplateField... fields) {
        List<TemplateField> list = new ArrayList<>(List.of(fields));
        return DocumentTemplate.builder()
                .templateName("TestTemplate")
                .documentFormat(DocumentFormat.DOCX)
                .documentContent(new byte[]{1})
                .fieldCount(list.size())
                .isFullyMapped(true)
                .templateFields(list)
                .build();
    }

    private static TemplateField requiredField(String label) {
        return TemplateField.builder()
                .fieldLabel(label)
                .isRequired(true)
                .fieldPosition(0)
                .build();
    }

    private static TemplateField optionalField(String label) {
        return TemplateField.builder()
                .fieldLabel(label)
                .isRequired(false)
                .fieldPosition(0)
                .build();
    }
}
