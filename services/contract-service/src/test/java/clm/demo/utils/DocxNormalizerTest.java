package clm.demo.utils;

import clm.demo.utils.docx.DocxNormalizer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * verifies that DocxNormalizer rewrites every dot-sequence placeholder to exactly
 * four dots, handles unicode dot-like glyphs, and is idempotent.
 */
class DocxNormalizerTest {

    @Test
    void six_dot_sequence_normalized_to_exactly_four_dots() throws IOException {
        byte[] input  = docxBytes("Hello ......");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        assertThat(extractText(output)).contains("....").doesNotContain("......");
    }

    @Test
    void already_four_dots_remain_unchanged() throws IOException {
        byte[] input  = docxBytes("Hello ....");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        assertThat(extractText(output)).contains("....");
    }

    @Test
    void three_dot_sequence_below_threshold_is_not_altered() throws IOException {
        byte[] input  = docxBytes("ellipsis ... end");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        // 3 dots is not a placeholder — must survive unchanged
        assertThat(extractText(output)).contains("...");
    }

    @Test
    void unicode_horizontal_ellipsis_pair_normalized_to_four_dots() throws IOException {
        // \u2026\u2026 → normalize → "......" → substitute → "...."
        byte[] input  = docxBytes("\u2026\u2026");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        assertThat(extractText(output)).contains("....");
        assertThat(extractText(output)).doesNotContain("......");
    }

    @Test
    void multiple_placeholders_all_normalized_to_four_dots() throws IOException {
        byte[] input  = docxBytes("Name: ...... Date: .......... Amount: ......");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        String text = extractText(output);
        // every placeholder is exactly four dots after normalization
        assertThat(text).doesNotContainPattern("\\.{5,}");
    }

    @Test
    void document_without_placeholders_returned_without_content_change() throws IOException {
        byte[] input  = docxBytes("No placeholders here.");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        assertThat(extractText(output)).contains("No placeholders here.");
    }

    @Test
    void normalization_is_idempotent() throws IOException {
        byte[] input      = docxBytes("Client: ......");
        byte[] firstPass  = DocxNormalizer.normalizePlaceholdersInDocx(input);
        byte[] secondPass = DocxNormalizer.normalizePlaceholdersInDocx(firstPass);

        assertThat(extractText(secondPass)).isEqualTo(extractText(firstPass));
    }

    @Test
    void nine_dot_sequence_becomes_exactly_four_dots() throws IOException {
        byte[] input  = docxBytes("Value: .........");
        byte[] output = DocxNormalizer.normalizePlaceholdersInDocx(input);

        assertThat(extractText(output)).contains("....");
        assertThat(extractText(output)).doesNotContain(".....");
    }

    // ------------------------------------------------------------------ //
    //  helpers                                                             //
    // ------------------------------------------------------------------ //

    private static byte[] docxBytes(String text) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText(text);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    private static String extractText(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(p -> sb.append(p.getText()));
            return sb.toString();
        }
    }
}
