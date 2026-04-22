package clm.demo.utils.file;

import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.UnsupportedConversionException;
import clm.demo.models.enums.DocumentFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility component for file compression and document format conversion.
 *
 * <p>Handles GZIP compression for database storage and bidirectional
 * conversion between DOCX and PDF via LibreOffice headless mode.</p>
 */
@Slf4j
@Component
public class FileUtils {

    private static final int BUFFER_SIZE = 65536; // 64KB

    private final String libreofficePath;

    public FileUtils(@Value("${libreoffice.path:libreoffice}") String libreofficePath) {
        this.libreofficePath = libreofficePath;
    }


    /**
     * Compresses raw byte data into GZIP format.
     *
     * @param data the uncompressed source bytes (e.g., a raw PDF or DOCX file)
     * @return a byte array containing the GZIP-compressed data
     * @throws IOException if a streaming or compression error occurs
     */
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos, BUFFER_SIZE)) {
            gzip.write(data);
            gzip.finish();
            return baos.toByteArray();
        }
    }

    /**
     * Decompresses GZIP-compressed data back to its original form.
     *
     * @param compressed the GZIP-encoded byte array retrieved from storage
     * @return the original uncompressed byte array
     * @throws IOException if the data is not valid GZIP format or is corrupted
     */
    public byte[] decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return new byte[0];

        try (GZIPInputStream gzip = new GZIPInputStream(
                new ByteArrayInputStream(compressed), BUFFER_SIZE)) {
            return gzip.readAllBytes();
        }
    }

    // -------------------------------------------------------------------------
    // Conversion
    // -------------------------------------------------------------------------

    /**
     * Converts document bytes from {@code sourceFormat} to {@code targetFormat}
     * using LibreOffice headless mode for both directions.
     *
     * @param data         raw bytes of the source document
     * @param sourceFormat the format the bytes are currently in
     * @param targetFormat the desired output format
     * @return converted document bytes, or the original bytes if formats are identical
     * @throws UnsupportedConversionException if the format combination is not supported
     * @throws FileConversionException        if reading, writing, or conversion fails
     */
    public byte[] convert(byte[] data, DocumentFormat sourceFormat, DocumentFormat targetFormat) {
        if (sourceFormat == targetFormat) return data;

        try {
            return switch (sourceFormat) {
                case DOCX -> {
                    if (targetFormat == DocumentFormat.PDF) yield convertWithLibreOffice(data, "docx", "pdf");
                    throw new UnsupportedConversionException(sourceFormat.name(), targetFormat.name());
                }
                case PDF -> {
                    if (targetFormat == DocumentFormat.DOCX) yield convertPdfToDocx(data);
                    throw new UnsupportedConversionException(sourceFormat.name(), targetFormat.name());
                }
            };
        } catch (UnsupportedConversionException e) {
            log.warn("Unsupported conversion requested: {} -> {}", sourceFormat, targetFormat);
            throw e;
        } catch (IOException e) {
            log.error("Conversion failed ({} -> {}): {}", sourceFormat, targetFormat, e.getMessage(), e);
            throw new FileConversionException(
                    "Conversion failed from %s to %s: %s".formatted(sourceFormat, targetFormat, e.getMessage()), e);
        } catch (Exception e) {
            log.error("Unexpected error during conversion ({} -> {}): {}", sourceFormat, targetFormat, e.getMessage(), e);
            throw new FileConversionException(
                    "Conversion failed from %s to %s: %s".formatted(sourceFormat, targetFormat, e.getMessage()), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a document using LibreOffice headless mode.
     *
     * <p>Writes the input bytes to a temp file, invokes LibreOffice with
     * {@code --convert-to <targetExt>}, reads the output file, then cleans up.</p>
     *
     * @param inputData       raw bytes of the source document
     * @param sourceExtension file extension of the source format (e.g. "docx", "pdf")
     * @param targetExtension file extension to convert to   (e.g. "pdf", "docx")
     * @return converted document bytes
     * @throws IOException if the process fails, is interrupted, or produces no output
     */
    private byte[] convertWithLibreOffice(byte[] inputData,
                                          String sourceExtension,
                                          String targetExtension) throws IOException {
        Path tempDir = Files.createTempDirectory("lo-convert-");
        try {
            Path inputFile = tempDir.resolve("input." + sourceExtension);
            Files.write(inputFile, inputData);

            ProcessBuilder pb = new ProcessBuilder(
                    libreofficePath,
                    "--headless",
                    "--convert-to", targetExtension,
                    "--outdir", tempDir.toString(),
                    inputFile.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());

            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("LibreOffice conversion interrupted", e);
            }

            if (exitCode != 0) {
                throw new IOException(
                        "LibreOffice exited with code %d (%s -> %s). Output: %s"
                                .formatted(exitCode, sourceExtension, targetExtension, output));
            }

            Path outputFile = tempDir.resolve("input." + targetExtension);
            if (!Files.exists(outputFile)) {
                throw new IOException(
                        "LibreOffice produced no output file (%s -> %s). Output: %s"
                                .formatted(sourceExtension, targetExtension, output));
            }

            byte[] result = Files.readAllBytes(outputFile);
            log.info("LibreOffice conversion successful ({} -> {}): {} bytes produced",
                    sourceExtension, targetExtension, result.length);
            return result;

        } finally {
            deleteTempDir(tempDir);
        }
    }

    /**
     * Recursively deletes a temporary directory, logging any failures without
     * propagating them — cleanup failures should never mask the real result.
     */
    private void deleteTempDir(Path tempDir) {
        try (var stream = Files.walk(tempDir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete temp file: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to clean up temp directory: {}", tempDir, e);
        }
    }

    /**
     * Converts a PDF to DOCX by extracting text and creating a DOCX document.
     * Uses Apache PDFBox to extract text and Apache POI to create the DOCX.
     *
     * @param pdfData the raw bytes of the PDF document
     * @return DOCX bytes containing the extracted text
     * @throws IOException if PDF reading or DOCX creation fails
     */
    private byte[] convertPdfToDocx(byte[] pdfData) throws IOException {
        try {
            PDDocument pdDocument = Loader.loadPDF(pdfData);
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(pdDocument);
            pdDocument.close();

            XWPFDocument docxDocument = new XWPFDocument();
            String[] paragraphs = extractedText.split("\n");

            for (String paragraphText : paragraphs) {
                if (!paragraphText.trim().isEmpty()) {
                    XWPFParagraph paragraph = docxDocument.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(paragraphText);
                }
            }

            ByteArrayOutputStream docxOutput = new ByteArrayOutputStream();
            docxDocument.write(docxOutput);
            docxDocument.close();

            byte[] docxBytes = docxOutput.toByteArray();
            log.info("PDF to DOCX conversion successful: {} bytes produced", docxBytes.length);
            return docxBytes;

        } catch (IOException e) {
            log.error("Failed to convert PDF to DOCX: {}", e.getMessage(), e);
            throw e;
        }
    }
}