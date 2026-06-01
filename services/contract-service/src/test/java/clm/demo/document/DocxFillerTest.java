package clm.demo.document;

import clm.demo.models.TemplateField;
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.docx.DocxNormalizer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DocxFiller} — verifies placeholder substitution in DOCX documents
 * without requiring LibreOffice or a real database.
 */
class DocxFillerTest {

    // ── fillDocx — basic substitution ─────────────────────────────────────────

    @Test
    void should_replace_single_placeholder_with_value() throws IOException {
        byte[] docx = docxWithText("Name: ....");
        TemplateField field = field("Name", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of("Name", "Alice"));

        assertThat(extractText(filled)).contains("Alice");
    }

    @Test
    void should_not_contain_dots_after_replacement() throws IOException {
        byte[] docx = docxWithText("Company: ....");
        TemplateField field = field("Company", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of("Company", "Acme Inc"));

        assertThat(extractText(filled)).doesNotContain("....");
        assertThat(extractText(filled)).contains("Acme Inc");
    }

    @Test
    void should_replace_multiple_placeholders_in_order() throws IOException {
        byte[] docx = docxWithText("First: ...., Second: ....");

        TemplateField f1 = field("First",  0);
        TemplateField f2 = field("Second", 1);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(f1, f2),
                Map.of("First", "AAA", "Second", "BBB"));

        String text = extractText(filled);
        assertThat(text).contains("AAA");
        assertThat(text).contains("BBB");
        assertThat(text).doesNotContain("....");
    }

    @Test
    void should_keep_original_dots_when_no_value_provided() throws IOException {
        byte[] docx = docxWithText("Name: ....");
        TemplateField field = field("Name", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of());

        // No value → dots stay in place
        assertThat(extractText(filled)).contains("....");
    }

    @Test
    void should_handle_empty_document_without_throwing() throws IOException {
        byte[] docx = emptyDocx();
        byte[] filled = DocxFiller.fillDocx(docx, List.of(), Map.of());
        assertThat(filled).isNotEmpty();
    }

    @Test
    void should_handle_no_placeholder_in_document() throws IOException {
        byte[] docx = docxWithText("This document has no placeholder fields.");
        TemplateField field = field("Name", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of("Name", "Alice"));

        assertThat(extractText(filled)).contains("no placeholder fields");
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    void should_handle_special_characters_in_replacement_value() throws IOException {
        byte[] docx = docxWithText("Note: ....");
        TemplateField field = field("Note", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field),
                Map.of("Note", "O'Reilly & Associates — Est. 2001"));

        assertThat(extractText(filled)).contains("O'Reilly");
    }

    @Test
    void should_handle_unicode_replacement_value() throws IOException {
        byte[] docx = docxWithText("Client: ....");
        TemplateField field = field("Client", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field),
                Map.of("Client", "Müller GmbH"));

        assertThat(extractText(filled)).contains("Müller");
    }

    @Test
    void should_handle_very_long_replacement_value() throws IOException {
        String longValue = "X".repeat(5000);
        byte[] docx = docxWithText("Data: ....");
        TemplateField field = field("Data", 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of("Data", longValue));

        assertThat(extractText(filled)).contains("X".repeat(100));
    }

    @ParameterizedTest
    @MethodSource("fieldValueScenarios")
    void should_substitute_correctly_for_various_field_values(String label, String value) throws IOException {
        byte[] docx = docxWithText("Field: ....");
        TemplateField field = field(label, 0);

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of(label, value));

        assertThat(extractText(filled)).contains(value);
    }

    static Stream<Object[]> fieldValueScenarios() {
        return Stream.of(
                new Object[]{"Date",     "2026-01-01"},
                new Object[]{"Amount",   "10,000.00"},
                new Object[]{"Contract", "CLM-2026-001"},
                new Object[]{"Name",     "Jean-François Dupont"},
                new Object[]{"Email",    "user@example.com"}
        );
    }

    // ── Template-level: no un-rendered placeholders ────────────────────────────

    @Test
    void should_replace_all_placeholders_when_all_values_provided() throws IOException {
        byte[] docx = docxWithParagraphs(
                "Party A: ....",
                "Party B: ....",
                "Date: ...."
        );
        List<TemplateField> fields = List.of(
                field("PartyA", 0),
                field("PartyB", 1),
                field("Date",   2)
        );
        Map<String, String> values = Map.of(
                "PartyA", "Acme Corp",
                "PartyB", "Beta Ltd",
                "Date",   "2026-06-01"
        );

        byte[] filled = DocxFiller.fillDocx(docx, fields, values);
        String text = extractText(filled);

        assertThat(text).doesNotContain("....");
        assertThat(text).contains("Acme Corp", "Beta Ltd", "2026-06-01");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static byte[] docxWithText(String text) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(text);
            return toBytes(doc);
        }
    }

    private static byte[] docxWithParagraphs(String... paragraphs) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            for (String text : paragraphs) {
                XWPFParagraph para = doc.createParagraph();
                XWPFRun run = para.createRun();
                run.setText(text);
            }
            return toBytes(doc);
        }
    }

    private static byte[] emptyDocx() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            return toBytes(doc);
        }
    }

    private static byte[] toBytes(XWPFDocument doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        return out.toByteArray();
    }

    private static String extractText(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    private static TemplateField field(String label, int position) {
        return TemplateField.builder()
                .fieldLabel(label)
                .fieldPosition(position)
                .isRequired(true)
                .build();
    }
}
