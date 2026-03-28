package clm.demo.services;

import clm.demo.models.enums.DocumentFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses uploaded contract templates (DOCX or PDF) and extracts placeholder occurrences.
 * Placeholders are sequences of 2+ consecutive dots (e.g. {@code "......"}).
 *
 * <p>Workflow: upload → parse text → find placeholders → return full text + positions
 * so the frontend can render the document with inline clickable placeholder spans.</p>
 */
@Slf4j
@Service
public class FileService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\.{2,}");
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    /**
     * Parses the uploaded document and returns the full text alongside
     * all placeholder occurrences in document order.
     *
     * @param file   uploaded DOCX or PDF template
     * @param format document format, selects the parser
     * @return {@link ParsedDocumentResponse} with full text and placeholder list
     * @throws IOException              if the document cannot be read or parsed
     * @throws IllegalArgumentException if the file is invalid or too large
     */
    public ParsedDocumentResponse parseTemplate(MultipartFile file, DocumentFormat format)
            throws IOException {
        validateFile(file);
        String content = extractText(file, format);
        List<PlaceholderInfo> placeholders = findPlaceholders(content);

        return ParsedDocumentResponse.builder()
                .documentText(content)
                .placeholderCount(placeholders.size())
                .placeholders(placeholders)
                .build();
    }

    private String extractText(MultipartFile file, DocumentFormat format) throws IOException {
        return switch (format) {
            case PDF -> parsePdf(file);
            case DOCX -> parseDocx(file);
        };
    }

    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            log.info("Parsed PDF '{}': {} chars", file.getOriginalFilename(), text.length());
            return text;
        } catch (Exception e) {
            log.error("Failed to parse PDF '{}'", file.getOriginalFilename(), e);
            throw new IOException("Failed to parse PDF: " + e.getMessage(), e);
        }
    }

    private String parseDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (!text.isBlank()) sb.append(text).append("\n");
            }
            String content = sb.toString();
            log.info("Parsed DOCX '{}': {} chars", file.getOriginalFilename(), content.length());
            return content;
        } catch (Exception e) {
            log.error("Failed to parse DOCX '{}'", file.getOriginalFilename(), e);
            throw new IOException("Failed to parse DOCX: " + e.getMessage(), e);
        }
    }

    /**
     * Text holder detection
     * @param content of the file
     * @return parsed data
     */
    private List<PlaceholderInfo> findPlaceholders(String content) {
        List<PlaceholderInfo> results = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        int position = 0;

        while (matcher.find()) {
            results.add(PlaceholderInfo.builder()
                    .position(position++)
                    .placeholderText(matcher.group())
                    .startIndex(matcher.start())
                    .endIndex(matcher.end())
                    .build());
        }

        log.info("Found {} placeholders", results.size());
        return results;
    }

    /**
     * Validation
     * @param file to be validated
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File cannot be null or empty");
        if (file.getSize() > MAX_FILE_SIZE)
            throw new IllegalArgumentException("File exceeds 50MB limit: " + file.getOriginalFilename());
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("File must have a valid filename");
        log.debug("File validated: {} ({} bytes)", name, file.getSize());
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParsedDocumentResponse {
        /** Full plain-text content of the document for frontend rendering. */
        private String documentText;
        /** Total number of placeholders found. */
        private int placeholderCount;
        /** Ordered placeholder occurrences, each with char indices for span highlighting. */
        private List<PlaceholderInfo> placeholders;
    }

    /**
     * A single placeholder occurrence found during parsing.
     * {@code startIndex}/{@code endIndex} point into {@code ParsedDocumentResponse.documentText}
     * and are used by the frontend to render each {@code ......} as a clickable span.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlaceholderInfo {
        /** Zero-based occurrence index in document order. Maps to {@code TemplateField.fieldPosition}. */
        private int position;
        /** Exact dot sequence captured (e.g. {@code "......"}). Maps to {@code TemplateField.placeholderText}. */
        private String placeholderText;
        /** Start char index in {@code documentText}. */
        private int startIndex;
        /** End char index (exclusive) in {@code documentText}. */
        private int endIndex;
    }
}