package clm.demo.services.file.actions;

import clm.demo.models.enums.DocumentFormat;
import lombok.extern.slf4j.Slf4j;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.springframework.core.convert.ConversionException;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Service for converting between DOCX and PDF formats.
 *
 */
@Slf4j
@Service
public class FileConverterService {

    /**
     * Converts document bytes from {@code sourceFormat} to {@code targetFormat}.
     *
     * @param data         raw bytes of the source document
     * @param sourceFormat the format the bytes are currently in
     * @param targetFormat the desired output format
     * @return converted document bytes
     * @throws IOException              if reading, writing, or conversion fails
     * @throws IllegalArgumentException if the format combination is unsupported
     */
    public byte[] convert(byte[] data, DocumentFormat sourceFormat, DocumentFormat targetFormat) throws IOException {

        if (sourceFormat == targetFormat) {
            return data;
        }

        try {
            return switch (sourceFormat) {
                case DOCX -> {
                    if (targetFormat == DocumentFormat.PDF) {
                        yield convertDocxToPdf(data);
                    }
                    throw new IllegalArgumentException("Unsupported conversion: " + sourceFormat + " to " + targetFormat);
                }
                case PDF -> {
                    if (targetFormat == DocumentFormat.DOCX) {
                        yield convertPdfToDocx(data);
                    }
                    throw new IllegalArgumentException("Unsupported conversion: " + sourceFormat + " to " + targetFormat);
                }
            };
        } catch (IOException e) {
            log.error("Conversion failed ({} : {}): {}", sourceFormat, targetFormat, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            throw new IOException("Conversion failed from " + sourceFormat + " to " + targetFormat + ": " + e.getMessage(), e);
        }
    }

    /**
     * Converts DOCX bytes to PDF using the docx4j XSL-FO : Apache FOP pipeline.
     * @param docxData raw bytes of the DOCX document
     * @return PDF document bytes
     */
    private byte[] convertDocxToPdf(byte[] docxData) throws IOException {
        try {
            // load the DOCX into docx4j's in-memory object model.
            WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(docxData));

            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            Docx4J.toPDF(wordPackage, pdfOut);

            return  pdfOut.toByteArray();

        } catch (ConversionException e) {
            throw new IOException("docx4j PDF conversion failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Unexpected docx4j error during DOCX -> PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts text from a PDF using PDFBox and rebuilds it as a DOCX via POI.
     *
     */
    private byte[] convertPdfToDocx(byte[] pdfData) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(pdfData);
             XWPFDocument docx = new XWPFDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setParagraphEnd("\n\n");
            stripper.setSortByPosition(true);

            String fullText = stripper.getText(pdf);

            String[] paragraphs = fullText.split("\n{2,}");

            for (String para : paragraphs) {
                String trimmed = para.strip();
                trimmed = trimmed.replaceAll("[ \\t]*\\n[ \\t]*", " ").replaceAll(" {2,}", " ");
                if (trimmed.isEmpty()) continue;

                XWPFParagraph p = docx.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(trimmed);
                run.setFontFamily("Calibri");
                run.setFontSize(11);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            docx.write(baos);
            log.info("PDF : DOCX: produced {} bytes", baos.size());
            return baos.toByteArray();
        }
    }
}