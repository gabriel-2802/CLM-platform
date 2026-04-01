package clm.demo.utils;

import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.UnsupportedConversionException;
import clm.demo.models.enums.DocumentFormat;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.core.convert.ConversionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for binary data compression using the GZIP format.
 * * <p>Used to reduce the storage footprint of 'BYTEA' columns in the database,
 * particularly for large PDF and DOCX contract templates and generated outputs.</p>
 */
@UtilityClass
@Slf4j
public class FileUtils {

    private static final int BUFFER_SIZE = 65536; // 64KB

    /**
     * Compresses raw byte data into GZIP format.
     *
     * @param data the uncompressed source bytes (e.g., a raw PDF or DOCX file)
     * @return a byte array containing the GZIP-compressed data
     * @throws IOException if a streaming or compression error occurs
     */
    public static byte[] compress(byte[] data) throws IOException {
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
    public static byte[] decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return new byte[0];

        try (GZIPInputStream gzip = new GZIPInputStream(
                new ByteArrayInputStream(compressed), BUFFER_SIZE)) {
            return gzip.readAllBytes();
        }
    }

    /**
     * Converts document bytes from {@code sourceFormat} to {@code targetFormat}.
     *
     * @param data         raw bytes of the source document
     * @param sourceFormat the format the bytes are currently in
     * @param targetFormat the desired output format
     * @return converted document bytes, or the original bytes if formats are identical
     * @throws UnsupportedConversionException if the format combination is not supported
     * @throws FileConversionException        if reading, writing, or conversion fails
     */
    public static byte[] convert(byte[] data, DocumentFormat sourceFormat, DocumentFormat targetFormat) {
        if (sourceFormat == targetFormat) return data;

        try {
            return switch (sourceFormat) {
                case DOCX -> {
                    if (targetFormat == DocumentFormat.PDF) yield convertDocxToPdf(data);
                    throw new UnsupportedConversionException(sourceFormat.name(), targetFormat.name());
                }
                case PDF -> {
                    if (targetFormat == DocumentFormat.DOCX) yield convertPdfToDocx(data);
                    throw new UnsupportedConversionException(sourceFormat.name(), targetFormat.name());
                }
            };
        } catch (UnsupportedConversionException e) {
            log.warn("unsupported conversion requested: {} to {}", sourceFormat, targetFormat);
            throw e;
        } catch (IOException e) {
            log.error("conversion failed ({} -> {}): {}", sourceFormat, targetFormat, e.getMessage(), e);
            throw new FileConversionException("Conversion failed from " + sourceFormat + " to " + targetFormat + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("unexpected error during conversion ({} -> {}): {}", sourceFormat, targetFormat, e.getMessage(), e);
            throw new FileConversionException("Conversion failed from " + sourceFormat + " to " + targetFormat + ": " + e.getMessage(), e);
        }
    }

    /**
     * Converts DOCX bytes to PDF using the docx4j XSL-FO / Apache FOP pipeline.
     *
     * @param docxData raw bytes of the DOCX document
     * @return PDF document bytes
     * @throws IOException if reading, writing, or the docx4j conversion fails
     */
    private static byte[] convertDocxToPdf(byte[] docxData) throws IOException {
        try {
            WordprocessingMLPackage wordPackage = WordprocessingMLPackage
                    .load(new ByteArrayInputStream(docxData));

            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            Docx4J.toPDF(wordPackage, pdfOut);
            return pdfOut.toByteArray();

        } catch (ConversionException e) {
            throw new IOException("docx4j PDF conversion failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("unexpected docx4j error during DOCX → PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts text from a PDF using PDFBox and rebuilds it as a DOCX via Apache POI.
     * Note: this is a text-only conversion — formatting, images, and layout are not preserved.
     *
     * @param pdfData raw bytes of the PDF document
     * @return DOCX document bytes
     * @throws IOException if reading or writing fails
     */
    private static byte[] convertPdfToDocx(byte[] pdfData) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(pdfData);
             XWPFDocument docx = new XWPFDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setParagraphEnd("\n\n");
            stripper.setSortByPosition(true);

            String[] paragraphs = stripper.getText(pdf).split("\n{2,}");

            for (String para : paragraphs) {
                String trimmed = para.strip()
                        .replaceAll("[ \\t]*\\n[ \\t]*", " ")
                        .replaceAll(" {2,}", " ");
                if (trimmed.isEmpty()) continue;

                XWPFParagraph p = docx.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(trimmed);
                run.setFontFamily("Calibri");
                run.setFontSize(11);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            docx.write(baos);
            log.info("PDF → DOCX: produced {} bytes", baos.size());
            return baos.toByteArray();
        }
    }

}