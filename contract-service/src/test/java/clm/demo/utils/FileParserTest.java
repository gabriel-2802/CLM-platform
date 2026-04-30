package clm.demo.utils;

import clm.demo.exceptions.InvalidFileException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.file.FileParser;
import clm.demo.utils.file.FileParser.ParsedDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * verifies FileParser.parseTemplate: null/empty/oversized file rejection,
 * missing filename rejection, and correct placeholder counting for DOCX files.
 */
class FileParserTest {

    // ================================================================== //
    //  file validation                                                     //
    // ================================================================== //

    @Nested
    class Validation {

        @Test
        void null_file_throws_illegal_argument() {
            assertThatThrownBy(() -> FileParser.parseTemplate(null, DocumentFormat.DOCX))
                    .isInstanceOf(InvalidFileException.class);
        }

        @Test
        void empty_file_throws_illegal_argument() {
            MockMultipartFile empty = new MockMultipartFile(
                    "file", "template.docx", "application/octet-stream", new byte[0]);

            assertThatThrownBy(() -> FileParser.parseTemplate(empty, DocumentFormat.DOCX))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("null or empty");
        }

        @Test
        void null_filename_throws_illegal_argument() {
            MultipartFile file = new MockMultipartFile(
                    "file", null, "application/octet-stream", new byte[]{1, 2, 3});

            assertThatThrownBy(() -> FileParser.parseTemplate(file, DocumentFormat.DOCX))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("valid filename");
        }

        @Test
        void blank_filename_throws_illegal_argument() {
            MultipartFile file = new MockMultipartFile(
                    "file", "   ", "application/octet-stream", new byte[]{1, 2, 3});

            assertThatThrownBy(() -> FileParser.parseTemplate(file, DocumentFormat.DOCX))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("valid filename");
        }

        @Test
        void file_exceeding_50_mb_throws_illegal_argument() {
            // override getSize() to report > 50 MB without allocating memory
            MultipartFile oversized = new MockMultipartFile(
                    "file", "big.docx", "application/octet-stream", new byte[]{1}) {
                @Override public long    getSize()  { return 51L * 1024 * 1024; }
                @Override public boolean isEmpty()  { return false; }
            };

            assertThatThrownBy(() -> FileParser.parseTemplate(oversized, DocumentFormat.DOCX))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("50 MB");
        }
    }

    // ================================================================== //
    //  DOCX parsing                                                        //
    // ================================================================== //

    @Nested
    class DocxParsing {

        @Test
        void docx_with_no_placeholders_returns_zero_count() throws IOException {
            MockMultipartFile file = docxFile("Hello World", "template.docx");

            ParsedDocument result = FileParser.parseTemplate(file, DocumentFormat.DOCX);

            assertThat(result.placeholderCount()).isZero();
            assertThat(result.documentText()).contains("Hello World");
        }

        @Test
        void docx_with_one_placeholder_returns_count_of_one() throws IOException {
            MockMultipartFile file = docxFile("Client Name: ....", "template.docx");

            ParsedDocument result = FileParser.parseTemplate(file, DocumentFormat.DOCX);

            assertThat(result.placeholderCount()).isEqualTo(1);
        }

        @Test
        void docx_with_three_placeholders_returns_correct_count() throws IOException {
            MockMultipartFile file = docxFile("Name: .... Date: .... Amount: ....", "template.docx");

            ParsedDocument result = FileParser.parseTemplate(file, DocumentFormat.DOCX);

            assertThat(result.placeholderCount()).isEqualTo(3);
        }

        @Test
        void docx_text_is_normalized_in_returned_document_text() throws IOException {
            // unicode ellipsis in the document should be normalized to ASCII dots
            MockMultipartFile file = docxFile("Client: \u2026\u2026", "template.docx");

            ParsedDocument result = FileParser.parseTemplate(file, DocumentFormat.DOCX);

            // \u2026\u2026 → "......" → PlaceholderProcessor.normalize → "......"
            // which is counted as one placeholder (4+ dots)
            assertThat(result.placeholderCount()).isEqualTo(1);
        }

        @Test
        void invalid_docx_bytes_throw_io_exception() {
            MockMultipartFile invalid = new MockMultipartFile(
                    "file", "bad.docx", "application/octet-stream",
                    new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

            assertThatThrownBy(() -> FileParser.parseTemplate(invalid, DocumentFormat.DOCX))
                    .isInstanceOf(IOException.class);
        }
    }

    // ------------------------------------------------------------------ //
    //  helpers                                                             //
    // ------------------------------------------------------------------ //

    /** builds a MockMultipartFile backed by a real DOCX containing the given paragraph text. */
    private MockMultipartFile docxFile(String text, String filename) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText(text);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return new MockMultipartFile(
                    "file", filename,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    out.toByteArray());
        }
    }
}
