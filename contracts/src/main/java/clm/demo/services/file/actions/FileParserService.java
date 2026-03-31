package clm.demo.services.file.actions;

import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.models.enums.DocumentFormat;
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
import java.util.List;

/**
 * Parses uploaded contract templates (DOCX or PDF) and extracts placeholder occurrences.
 * Placeholders are sequences of 4+ consecutive dots (e.g. {@code "......"}).
 *
 * <p>Workflow: upload → validate → extract text → normalize → detect placeholders
 * → return full normalized text + positions so the frontend can render inline
 * clickable placeholder spans.</p>
 *
 * <p><strong>Important:</strong> {@code documentText} in the response is always the
 * <em>normalized</em> string (CRLF → LF). {@code startOffset}/{@code endOffset} on each
 * {@link ParsedTemplateResponseDTO.PlaceholderDTO} point into that same string, so frontend offsets are
 * always consistent.</p>
 */
@Slf4j
@Service
public class FileParserService {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50 MB

    /**
     * Parses the uploaded document and returns the full normalized text alongside
     * all placeholder occurrences in document order.
     *
     * @param file   uploaded DOCX or PDF template
     * @param format document format, selects the parser
     * @return {@link ParsedDocumentResponse} with normalized text and placeholder list
     * @throws IOException              if the document cannot be read or parsed
     * @throws IllegalArgumentException if the file is invalid or too large
     */
    public ParsedDocumentResponse parseTemplate(MultipartFile file, DocumentFormat format)
            throws IOException {
        validateFile(file);

        String raw = extractText(file, format);
        String normalized = PlaceholderProcessor.normalize(raw);
        List<ParsedTemplateResponseDTO.PlaceholderDTO> placeholders = PlaceholderProcessor.findPlaceholders(normalized)
                .stream()
                .map(record -> ParsedTemplateResponseDTO.PlaceholderDTO.builder()
                        .position(record.occurrenceIndex())
                        .placeholderText(record.prevText())
                        .startIndex(record.startOffset())
                        .endIndex(record.endOffset())
                        .fieldId(-1L)
                        .build())
                .toList();

        return ParsedDocumentResponse.builder()
                .documentText(normalized)
                .placeholderCount(placeholders.size())
                .placeholders(placeholders)
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
            log.info("Parsed PDF '{}': {} chars", file.getOriginalFilename(), text.length());
            return text;
        } catch (Exception e) {
            log.error("Failed to parse PDF '{}'", file.getOriginalFilename(), e);
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

            // Body paragraphs — preserve blank lines to keep char offsets accurate
            document.getParagraphs()
                    .forEach(p -> sb.append(p.getText()).append("\n"));

            // Table cells
            document.getTables().forEach(table ->
                    table.getRows().forEach(row ->
                            row.getTableCells().forEach(cell ->
                                    cell.getParagraphs().forEach(p ->
                                            sb.append(p.getText()).append("\n")))));

            // Headers
            document.getHeaderList().forEach(header ->
                    header.getParagraphs().forEach(p ->
                            sb.append(p.getText()).append("\n")));

            // Footers
            document.getFooterList().forEach(footer ->
                    footer.getParagraphs().forEach(p ->
                            sb.append(p.getText()).append("\n")));

            String content = sb.toString();
            log.info("Parsed DOCX '{}': {} chars", file.getOriginalFilename(), content.length());
            return content;
        } catch (Exception e) {
            log.error("Failed to parse DOCX '{}'", file.getOriginalFilename(), e);
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

        log.debug("File validated: {} ({} bytes)", name, file.getSize());
    }

    // -------------------------------------------------------------------------
    // Response model
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedDocumentResponse {
        /** Full normalized (CRLF → LF) plain-text content for frontend rendering. */
        private String documentText;
        /** Total number of placeholders found. */
        private int placeholderCount;
        /**
         * Ordered placeholder occurrences. {@code startOffset}/{@code endOffset} on each
         * entry point into {@code documentText} and are used by the frontend to render
         * each dot sequence as a clickable span.
         */
        private List<ParsedTemplateResponseDTO.PlaceholderDTO> placeholders;
    }
}