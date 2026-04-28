package clm.demo.services;

import clm.demo.exceptions.MissingMandatoryFieldException;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DataType;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentGenerationUtilTest {

    private DocumentGenerationUtil util;

    @BeforeEach
    void setUp() {
        util = new DocumentGenerationUtil();
    }

    // ================================================================== //
    //  validateMandatoryFields                                             //
    // ================================================================== //

    @Nested
    class ValidateMandatoryFields {

        @Test
        void all_required_fields_present_passes_without_exception() {
            DocumentTemplate template = templateWith(requiredField("Client Name"), requiredField("Date"));
            Map<String, String> mappings = Map.of("Client Name", "Acme", "Date", "2026-01-01");

            // no exception expected
            util.validateMandatoryFields(template, mappings);
        }

        @Test
        void missing_required_field_throws_with_field_name() {
            DocumentTemplate template = templateWith(requiredField("Client Name"), requiredField("Date"));
            Map<String, String> mappings = Map.of("Client Name", "Acme"); // "Date" is absent

            assertThatThrownBy(() -> util.validateMandatoryFields(template, mappings))
                    .isInstanceOf(MissingMandatoryFieldException.class)
                    .satisfies(ex -> {
                        MissingMandatoryFieldException mex = (MissingMandatoryFieldException) ex;
                        assertThat(mex.getMissingFields()).containsExactly("Date");
                    });
        }

        @Test
        void blank_value_for_required_field_throws() {
            DocumentTemplate template = templateWith(requiredField("Client Name"));
            Map<String, String> mappings = Map.of("Client Name", "   "); // blank, not absent

            assertThatThrownBy(() -> util.validateMandatoryFields(template, mappings))
                    .isInstanceOf(MissingMandatoryFieldException.class);
        }

        @Test
        void optional_field_missing_does_not_throw() {
            TemplateField optional = TemplateField.builder()
                    .fieldLabel("Notes")
                    .dataType(DataType.STRING)
                    .isRequired(false)
                    .fieldPosition(0)
                    .build();
            DocumentTemplate template = templateWith(optional);
            Map<String, String> mappings = Map.of(); // notes absent — fine

            util.validateMandatoryFields(template, mappings); // no exception
        }

        @Test
        void field_with_null_label_is_ignored_during_validation() {
            TemplateField unmapped = TemplateField.builder()
                    .fieldLabel(null) // not yet mapped by the admin
                    .dataType(DataType.STRING)
                    .isRequired(true)
                    .fieldPosition(0)
                    .build();
            DocumentTemplate template = templateWith(unmapped);

            util.validateMandatoryFields(template, Map.of()); // no exception
        }

        @Test
        void multiple_missing_fields_all_reported() {
            DocumentTemplate template = templateWith(
                    requiredField("A"), requiredField("B"), requiredField("C"));

            assertThatThrownBy(() -> util.validateMandatoryFields(template, Map.of()))
                    .isInstanceOf(MissingMandatoryFieldException.class)
                    .satisfies(ex -> {
                        List<String> missing = ((MissingMandatoryFieldException) ex).getMissingFields();
                        assertThat(missing).containsExactlyInAnyOrder("A", "B", "C");
                    });
        }
    }

    // ================================================================== //
    //  buildFieldValues                                                    //
    // ================================================================== //

    @Nested
    class BuildFieldValues {

        @Test
        void builds_one_field_value_per_mapped_label() {
            DocumentTemplate template = templateWith(requiredField("Client Name"), requiredField("Date"));
            Contract doc = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);
            Map<String, String> mappings = Map.of("Client Name", "Acme", "Date", "2026-01-01");

            List<DocumentFieldValue> result = util.buildFieldValues(doc, template, mappings);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(DocumentFieldValue::getFieldValue)
                    .containsExactlyInAnyOrder("Acme", "2026-01-01");
        }

        @Test
        void field_with_null_label_is_skipped() {
            TemplateField unmapped = TemplateField.builder()
                    .fieldLabel(null)
                    .dataType(DataType.STRING)
                    .isRequired(false)
                    .fieldPosition(0)
                    .build();
            DocumentTemplate template = templateWith(unmapped);
            Contract doc = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);

            List<DocumentFieldValue> result = util.buildFieldValues(doc, template, Map.of("anything", "x"));

            assertThat(result).isEmpty();
        }

        @Test
        void blank_value_in_mappings_is_skipped() {
            DocumentTemplate template = templateWith(requiredField("Client Name"));
            Contract doc = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);

            List<DocumentFieldValue> result = util.buildFieldValues(doc, template, Map.of("Client Name", "  "));

            assertThat(result).isEmpty();
        }

        @Test
        void field_not_in_mappings_is_skipped() {
            DocumentTemplate template = templateWith(requiredField("Client Name"));
            Contract doc = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);

            List<DocumentFieldValue> result = util.buildFieldValues(doc, template, Map.of("Other", "x"));

            assertThat(result).isEmpty();
        }

        @Test
        void document_reference_set_on_every_field_value() {
            DocumentTemplate template = templateWith(requiredField("Client Name"));
            Contract doc = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);

            List<DocumentFieldValue> result = util.buildFieldValues(
                    doc, template, Map.of("Client Name", "Acme"));

            assertThat(result).allMatch(fv -> fv.getDocument() == doc);
        }
    }

    // ================================================================== //
    //  buildLabelValueMap                                                  //
    // ================================================================== //

    @Nested
    class BuildLabelValueMap {

        @Test
        void empty_list_returns_empty_map() {
            assertThat(util.buildLabelValueMap(List.of())).isEmpty();
        }

        @Test
        void builds_map_from_field_values() {
            List<DocumentFieldValue> fvs = List.of(
                    TestDataFactory.fieldValue(1L, "Client Name", "Acme"),
                    TestDataFactory.fieldValue(2L, "Date", "2026-01-01")
            );

            Map<String, String> map = util.buildLabelValueMap(fvs);

            assertThat(map).containsEntry("Client Name", "Acme")
                           .containsEntry("Date", "2026-01-01");
        }

        @Test
        void field_value_with_null_template_field_is_skipped() {
            DocumentFieldValue fv = DocumentFieldValue.builder()
                    .id(1L)
                    .templateField(null) // no template field
                    .fieldValue("x")
                    .build();

            assertThat(util.buildLabelValueMap(List.of(fv))).isEmpty();
        }

        @Test
        void field_value_with_null_label_is_skipped() {
            TemplateField noLabel = TemplateField.builder()
                    .fieldLabel(null)
                    .dataType(DataType.STRING)
                    .isRequired(false)
                    .build();
            DocumentFieldValue fv = DocumentFieldValue.builder()
                    .id(1L)
                    .templateField(noLabel)
                    .fieldValue("x")
                    .build();

            assertThat(util.buildLabelValueMap(List.of(fv))).isEmpty();
        }
    }

    // ------------------------------------------------------------------ //
    //  helpers                                                             //
    // ------------------------------------------------------------------ //

    private static TemplateField requiredField(String label) {
        return TemplateField.builder()
                .fieldLabel(label)
                .dataType(DataType.STRING)
                .isRequired(true)
                .fieldPosition(0)
                .build();
    }

    private static DocumentTemplate templateWith(TemplateField... fields) {
        return DocumentTemplate.builder()
                .templateName("test-template")
                .documentFormat(clm.demo.models.enums.DocumentFormat.DOCX)
                .documentContent(new byte[]{1, 2, 3})
                .fieldCount(fields.length)
                .isFullyMapped(true)
                .templateFields(new ArrayList<>(List.of(fields)))
                .build();
    }
}
