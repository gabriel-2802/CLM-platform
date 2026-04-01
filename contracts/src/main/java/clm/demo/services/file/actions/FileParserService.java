package clm.demo.services.file.actions;

import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.DocxTraversal;
import clm.demo.utils.PlaceholderProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Parses uploaded contract templates (DOCX or PDF) and extracts placeholder count.
 * Placeholders are sequences of 4+ consecutive dots (e.g. {@code "......"}).
 *
 * <p>Workflow: upload → validate → extract text → normalize → count placeholders
 * → return normalized text and count so the service layer can create field entities.</p>
 *
 * <p><strong>Important:</strong> {@code documentText} in the response is always the
 * <em>normalized</em> string (CRLF → LF). Placeholder positions are derived from regex
 * matching order, not stored explicitly.</p>
 */
@Slf4j
@Service
public class FileParserService {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50 MB

    /**
     * Parses the uploaded document and returns the full normalized text and placeholder count.
     *
     * @param file   uploaded DOCX or PDF template
     * @param format document format, selects the parser
     * @return {@link ParsedDocument} with normalized text and placeholder count
     * @throws IOException              if the document cannot be read or parsed
     * @throws IllegalArgumentException if the file is invalid or too large
     */
    public ParsedDocument parseTemplate(MultipartFile file, DocumentFormat format)
            throws IOException {
        validateFile(file);

        String raw = extractText(file, format);
        String normalized = PlaceholderProcessor.normalize(raw);
        int placeholderCount = PlaceholderProcessor.findPlaceholders(normalized).size();

        return ParsedDocument.builder()
                .documentText(normalized)
                .placeholderCount(placeholderCount)
                .build();
    }

    // -------------------------------------------------------------------------
    // Text extraction
    // -------------------------------------------------------------------------

    private String extractText(MultipartFile file, DocumentFormat format) throws IOException {
        return switch (format) {
            case PDF  -> extractPdf(file);
            case DOCX -> extractDocx(file);
        };
    }

    private String extractPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            log.info("parsed PDF '{}': {} chars", file.getOriginalFilename(), text.length());
            return text;
        } catch (Exception e) {
            log.error("failed to parse PDF '{}'", file.getOriginalFilename(), e);
            throw new IOException("Failed to parse PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts text from all content zones in the DOCX:
     * body paragraphs, table cells, headers, and footers.
     *
     * <p>Blank paragraphs are preserved as empty lines so that character offsets
     * remain accurate — skipping them would shift every subsequent
     * {@code startOffset}/{@code endOffset}.</p>
     */
    private String extractDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            // Preserve blank lines as empty entries to keep placeholder offsets accurate.
            DocxTraversal.forEachParagraph(document, p -> sb.append(p.getText()).append("\n"));

            String content = sb.toString();
            log.info("parsed DOCX '{}': {} chars", file.getOriginalFilename(), content.length());
            return content;
        } catch (Exception e) {
            log.error("failed to parse DOCX '{}'", file.getOriginalFilename(), e);
            throw new IOException("Failed to parse DOCX: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File cannot be null or empty");
        if (file.getSize() > MAX_FILE_SIZE)
            throw new IllegalArgumentException("File exceeds 50 MB limit: " + file.getOriginalFilename());

        String name = file.getOriginalFilename();
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("File must have a valid filename");

        log.debug("file validated: {} ({} bytes)", name, file.getSize());
    }

    // -------------------------------------------------------------------------
    // Response model
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedDocument {
        /** Full normalized (CRLF → LF) plain-text content. */
        private String documentText;
        /** Total number of placeholders found. */
        private int placeholderCount;
    }
}