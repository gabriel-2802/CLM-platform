package clm.demo.utils.file;

import clm.demo.exceptions.exceptions.InvalidFileException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.docx.DocxUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Parses uploaded contract templates (DOCX or PDF) and extracts placeholder count.
 * Placeholders are sequences of 4+ consecutive dots (e.g. {@code "......"}).
 */
@Slf4j
@UtilityClass
public class FileParser {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50 MB

    /**
     * Parses the uploaded document and returns the full normalized text
     * and placeholder count.
     *
     * @param file   uploaded DOCX or PDF template
     * @param format document format, selects the parser
     * @return {@link ParsedDocument} with normalized text and placeholder count
     * @throws IOException          if the document cannot be read or parsed
     * @throws InvalidFileException if the file is null, empty, too large,
     *                              or has an invalid filename
     */
    public ParsedDocument parseTemplate(MultipartFile file, DocumentFormat format)
            throws IOException {
        validateFile(file);

        String raw = extractText(file, format);
        String normalized = PlaceholderProcessor.normalize(raw);
        int placeholderCount = PlaceholderProcessor.findPlaceholders(normalized).size();

        return new ParsedDocument(normalized, placeholderCount);
    }

    // -------------------------------------------------------------------------
    // Private extraction helpers
    // -------------------------------------------------------------------------

    private String extractText(MultipartFile file, DocumentFormat format) throws IOException {
        return switch (format) {
            case PDF  -> extractPdf(file);
            case DOCX -> extractDocx(file);
            default   -> throw new InvalidFileException(
                    "Unsupported document format: " + format);
        };
    }

    private String extractPdf(MultipartFile file) throws IOException {
        try (
                InputStream inputStream = file.getInputStream();
                PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))
        ) {
            String text = new PDFTextStripper().getText(document);
            log.debug("Parsed PDF '{}': {} chars", file.getOriginalFilename(), text.length());
            return text;
        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse PDF '{}'", file.getOriginalFilename(), e);
            throw new IOException("Failed to parse PDF document.", e);
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
            DocxUtils.forEachParagraph(document, p -> sb.append(p.getText()).append("\n"));

            String content = sb.toString();
            log.debug("Parsed DOCX '{}': {} chars", file.getOriginalFilename(), content.length());
            return content;
        } catch (Exception e) {
            log.error("Failed to parse DOCX '{}'", file.getOriginalFilename(), e);
            throw new IOException("Failed to parse DOCX document.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new InvalidFileException("File must not be null or empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    "File exceeds the 50 MB size limit: " + file.getOriginalFilename());
        }

        String name = file.getOriginalFilename();
        if (Objects.isNull(name) || name.isBlank()) {
            throw new InvalidFileException("File must have a valid filename.");
        }

        log.debug("File validated: '{}' ({} bytes)", name, file.getSize());
    }

    /**
     * Immutable result of a parsed document template.
     *
     * @param documentText     full normalized text extracted from the document
     * @param placeholderCount number of placeholder sequences (4+ consecutive dots) found
     */
    public record ParsedDocument(
            String documentText,
            int placeholderCount) {
    }
}