package clm.demo.utils;

import clm.demo.models.TemplateField;
import clm.demo.models.enums.DataType;
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.docx.DocxNormalizer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * verifies DocxFiller.fillDocx: placeholder filling, partial fills, missing label
 * handling, and the empty-field-list short-circuit.
 * every test normalizes the DOCX first to mirror the real production flow
 * (template upload normalizes once; fill is called at contract generation time).
 */
class DocxFillerTest {

    @Test
    void single_placeholder_replaced_with_mapped_value() throws IOException {
        TemplateField field = field("Client Name", 0);
        byte[] docx = normalized(docxBytes("Name: ...."));

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of("Client Name", "Acme Corp"));

        assertThat(text(filled)).contains("Acme Corp");
        assertThat(text(filled)).doesNotContain("....");
    }

    @Test
    void multiple_placeholders_all_filled_in_position_order() throws IOException {
        TemplateField f1 = field("Name",   0);
        TemplateField f2 = field("Amount", 1);
        byte[] docx = normalized(docxBytes("Name: .... Amount: ...."));

        byte[] filled = DocxFiller.fillDocx(
                docx, List.of(f1, f2),
                Map.of("Name", "Alice", "Amount", "1000"));

        String result = text(filled);
        assertThat(result).contains("Alice").contains("1000");
        assertThat(result).doesNotContain("....");
    }

    @Test
    void label_absent_from_map_leaves_placeholder_intact() throws IOException {
        TemplateField field = field("Missing", 0);
        byte[] docx = normalized(docxBytes("Value: ...."));

        byte[] filled = DocxFiller.fillDocx(docx, List.of(field), Map.of());

        assertThat(text(filled)).contains("....");
    }

    @Test
    void empty_field_list_leaves_document_completely_unchanged() throws IOException {
        byte[] docx = normalized(docxBytes("Value: ...."));

        byte[] filled = DocxFiller.fillDocx(docx, List.of(), Map.of());

        assertThat(text(filled)).contains("....");
    }

    @Test
    void only_first_placeholder_filled_when_second_label_missing() throws IOException {
        TemplateField f1 = field("Name",   0);
        TemplateField f2 = field("Amount", 1);
        byte[] docx = normalized(docxBytes("Name: .... Amount: ...."));

        byte[] filled = DocxFiller.fillDocx(
                docx, List.of(f1, f2),
                Map.of("Name", "Bob"));   // "Amount" intentionally absent

        String result = text(filled);
        assertThat(result).contains("Bob");
        assertThat(result).contains("....");   // second placeholder intact
    }

    @Test
    void empty_paragraph_before_placeholder_does_not_throw() throws IOException {
        // first paragraph is empty, second contains a placeholder
        byte[] docx;
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph();                              // empty
            doc.createParagraph().createRun().setText("Val: ....");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            docx = out.toByteArray();
        }
        byte[] normalizedDocx = normalized(docx);
        TemplateField field = field("Val", 0);

        byte[] filled = DocxFiller.fillDocx(normalizedDocx, List.of(field), Map.of("Val", "OK"));

        assertThat(text(filled)).contains("OK");
    }

    @Test
    void placeholder_in_table_cell_is_filled() throws IOException {
        byte[] docx;
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createTable(1, 1).getRow(0).getCell(0)
                    .getParagraphs().getFirst().createRun().setText("Cell: ....");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            docx = out.toByteArray();
        }
        byte[] normalizedDocx = normalized(docx);
        TemplateField field = field("Cell", 0);

        byte[] filled = DocxFiller.fillDocx(normalizedDocx, List.of(field), Map.of("Cell", "Data"));

        // read table cell text from the filled document
        try (XWPFDocument result = new XWPFDocument(new ByteArrayInputStream(filled))) {
            String cellText = result.getTables().getFirst()
                    .getRow(0).getCell(0).getText();
            assertThat(cellText).contains("Data");
        }
    }

    @Test
    void more_fields_than_placeholders_does_not_throw() throws IOException {
        // only 1 placeholder but 2 fields — second field is silently ignored
        TemplateField f1 = field("A", 0);
        TemplateField f2 = field("B", 1);
        byte[] docx = normalized(docxBytes("Only: ...."));

        byte[] filled = DocxFiller.fillDocx(
                docx, List.of(f1, f2),
                Map.of("A", "X", "B", "Y"));

        assertThat(text(filled)).contains("X");
    }

    // ------------------------------------------------------------------ //
    //  helpers                                                             //
    // ------------------------------------------------------------------ //

    private TemplateField field(String label, int position) {
        return TemplateField.builder()
                .id((long) (position + 1))
                .fieldLabel(label)
                .dataType(DataType.STRING)
                .fieldPosition(position)
                .isRequired(false)
                .build();
    }

    private byte[] docxBytes(String text) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText(text);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    /** mirrors the real production flow: templates are normalized at upload time. */
    private byte[] normalized(byte[] docxBytes) throws IOException {
        return DocxNormalizer.normalizePlaceholdersInDocx(docxBytes);
    }

    private String text(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(p -> sb.append(p.getText()));
            return sb.toString();
        }
    }
}
